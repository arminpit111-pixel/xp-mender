package de.xpmender;

import java.util.ArrayList;
import java.util.List;

/**
 * Verteilt eine Anzahl XP-Bottles so auf Ruestungsteile, dass die verbleibende
 * Beschaedigung moeglichst gleich hoch ist ("Wasserstand"-Verfahren):
 * Das am staerksten beschaedigte Teil bekommt zuerst Bottles, bis es auf das
 * Niveau des naechsten Teils abgesunken ist usw. Reichen die Bottles fuer alles,
 * wird alles komplett repariert und der Rest bleibt uebrig.
 *
 * Reine Rechenlogik, keine Minecraft-Abhaengigkeit.
 */
public final class MendingCalculator {

    private MendingCalculator() {}

    /** damage = fehlende Haltbarkeit, maxDamage = maximale Haltbarkeit. */
    public record Piece(String name, int damage, int maxDamage) {}

    public record Allocation(Piece piece, int bottles) {}

    public record Plan(List<Allocation> allocations, int used, int leftover) {}

    /**
     * @param xpPerBottle erwartete XP pro Bottle (vanilla: 3-11, Schnitt 7). Mending repariert 2 Haltbarkeit pro XP.
     * @param percent     true = Prozent der max. Haltbarkeit angleichen, false = absolute fehlende Haltbarkeit angleichen
     */
    public static Plan calculate(List<Piece> pieces, int bottles, int xpPerBottle, boolean percent) {
        int n = pieces.size();
        int[] counts = new int[n];
        double per = Math.max(1, xpPerBottle) * 2.0;

        if (n > 0 && bottles > 0) {
            double level;
            if (total(pieces, 0.0, per, percent) <= bottles) {
                level = 0.0; // genug Bottles, um alles komplett zu reparieren
            } else {
                double lo = 0.0;
                double hi = percent ? 1.0 : maxDamageOf(pieces);
                for (int i = 0; i < 100; i++) {
                    double mid = (lo + hi) / 2.0;
                    if (total(pieces, mid, per, percent) <= bottles) {
                        hi = mid;
                    } else {
                        lo = mid;
                    }
                }
                level = hi;
            }

            int used = 0;
            for (int i = 0; i < n; i++) {
                counts[i] = needed(pieces.get(i), level, per, percent);
                used += counts[i];
            }

            // Uebrige Bottles (durch Aufrunden entstanden) an das Teil mit dem meisten Restschaden geben
            while (used < bottles) {
                int best = -1;
                double bestRemaining = 0.0;
                for (int i = 0; i < n; i++) {
                    Piece p = pieces.get(i);
                    double remaining = p.damage() - counts[i] * per;
                    if (percent && p.maxDamage() > 0) {
                        remaining /= p.maxDamage();
                    }
                    if (remaining > bestRemaining + 1e-9) {
                        bestRemaining = remaining;
                        best = i;
                    }
                }
                if (best < 0) {
                    break; // alles voll repariert
                }
                counts[best]++;
                used++;
            }
        }

        List<Allocation> allocations = new ArrayList<>();
        int used = 0;
        for (int i = 0; i < n; i++) {
            allocations.add(new Allocation(pieces.get(i), counts[i]));
            used += counts[i];
        }
        return new Plan(allocations, used, Math.max(0, bottles - used));
    }

    private static double maxDamageOf(List<Piece> pieces) {
        double max = 0;
        for (Piece p : pieces) {
            max = Math.max(max, p.damage());
        }
        return max;
    }

    private static int total(List<Piece> pieces, double level, double per, boolean percent) {
        int sum = 0;
        for (Piece p : pieces) {
            sum += needed(p, level, per, percent);
        }
        return sum;
    }

    private static int needed(Piece p, double level, double per, boolean percent) {
        double target = percent ? level * p.maxDamage() : level;
        double need = p.damage() - target;
        if (need <= 0) {
            return 0;
        }
        return (int) Math.ceil(need / per - 1e-9);
    }
}
