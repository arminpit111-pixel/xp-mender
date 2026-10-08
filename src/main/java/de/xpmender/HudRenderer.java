package de.xpmender;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class HudRenderer {

    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREEN = 0xFF55FF55;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int LINE_HEIGHT = 11;
    private static final int PADDING = 3;

    private HudRenderer() {}

    public record Line(String text, int color) {}

    /**
     * Zeichnet das HUD an der konfigurierten Position.
     * @param preview true = im Config-Menue: ohne echte Daten werden Beispielwerte gezeigt
     * @return {Breite, Hoehe} des Textbereichs (ohne Padding), {0,0} wenn nichts gezeichnet wurde
     */
    public static int[] render(DrawContext ctx, boolean preview) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ModConfig cfg = ModConfig.get();
        List<Line> lines = buildLines(mc, cfg, preview);
        if (lines.isEmpty()) {
            return new int[] {0, 0};
        }

        TextRenderer tr = mc.textRenderer;
        int w = 0;
        for (Line l : lines) {
            w = Math.max(w, tr.getWidth(l.text()));
        }
        int h = lines.size() * LINE_HEIGHT - 2;
        int x = cfg.hudX;
        int y = cfg.hudY;

        if (cfg.showBackground) {
            ctx.fill(x - PADDING, y - PADDING, x + w + PADDING, y + h + PADDING, 0x90000000);
        }
        for (int i = 0; i < lines.size(); i++) {
            Line l = lines.get(i);
            ctx.drawTextWithShadow(tr, Text.literal(l.text()), x, y + i * LINE_HEIGHT, l.color());
        }
        return new int[] {w, h};
    }

    public static int padding() {
        return PADDING;
    }

    private static List<Line> buildLines(MinecraftClient mc, ModConfig cfg, boolean preview) {
        List<Line> out = new ArrayList<>();
        ClientPlayerEntity player = mc.player;

        int bottles = 0;
        List<MendingCalculator.Piece> pieces = new ArrayList<>();

        if (player != null && mc.world != null) {
            PlayerInventory inv = player.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack s = inv.getStack(i);
                if (s.isOf(Items.EXPERIENCE_BOTTLE)) {
                    bottles += s.getCount();
                }
            }

            RegistryEntry<Enchantment> mending = mc.world.getRegistryManager()
                    .getOrThrow(RegistryKeys.ENCHANTMENT)
                    .getOrThrow(Enchantments.MENDING);

            addIfMending(pieces, "Helm", player.getEquippedStack(EquipmentSlot.HEAD), mending);
            addIfMending(pieces, "Brust", player.getEquippedStack(EquipmentSlot.CHEST), mending);
            addIfMending(pieces, "Hose", player.getEquippedStack(EquipmentSlot.LEGS), mending);
            addIfMending(pieces, "Schuhe", player.getEquippedStack(EquipmentSlot.FEET), mending);
            if (cfg.includeMainHand) {
                addIfMending(pieces, "Haupthand", player.getEquippedStack(EquipmentSlot.MAINHAND), mending);
            }
            if (cfg.includeOffHand) {
                addIfMending(pieces, "Offhand", player.getEquippedStack(EquipmentSlot.OFFHAND), mending);
            }
        }

        boolean sample = false;
        if (preview && (bottles == 0 || pieces.isEmpty())) {
            // Beispieldaten fuers Verschieben im Menue
            sample = true;
            bottles = 128;
            pieces.clear();
            pieces.add(new MendingCalculator.Piece("Helm", 227, 407));
            pieces.add(new MendingCalculator.Piece("Brust", 192, 592));
            pieces.add(new MendingCalculator.Piece("Hose", 405, 555));
            pieces.add(new MendingCalculator.Piece("Schuhe", 300, 481));
        }

        if (bottles == 0 && !sample) {
            return out; // keine Bottles -> HUD bleibt unsichtbar
        }

        out.add(new Line("XP-Bottles: " + bottles + (sample ? " (Beispiel)" : ""), YELLOW));

        if (pieces.isEmpty()) {
            out.add(new Line("Keine beschädigte Mending-Rüstung", GRAY));
            return out;
        }

        MendingCalculator.Plan plan = MendingCalculator.calculate(pieces, bottles, cfg.xpPerBottle, cfg.percentMode);
        double per = cfg.xpPerBottle * 2.0;

        for (MendingCalculator.Allocation a : plan.allocations()) {
            MendingCalculator.Piece p = a.piece();
            StringBuilder sb = new StringBuilder();
            sb.append(p.name()).append(": ").append(a.bottles()).append("x");
            if (cfg.showDurability) {
                int now = p.maxDamage() - p.damage();
                int after = (int) Math.min(p.maxDamage(), now + a.bottles() * per);
                sb.append("  (").append(now).append(" > ").append(after).append("/").append(p.maxDamage()).append(")");
            }
            out.add(new Line(sb.toString(), a.bottles() > 0 ? GREEN : GRAY));
        }
        if (plan.leftover() > 0) {
            out.add(new Line("Übrig: " + plan.leftover(), WHITE));
        }
        return out;
    }

    private static void addIfMending(List<MendingCalculator.Piece> pieces, String name, ItemStack stack,
                                     RegistryEntry<Enchantment> mending) {
        if (stack.isEmpty() || !stack.isDamageable() || stack.getDamage() <= 0) {
            return;
        }
        if (EnchantmentHelper.getLevel(mending, stack) <= 0) {
            return;
        }
        pieces.add(new MendingCalculator.Piece(name, stack.getDamage(), stack.getMaxDamage()));
    }
}
