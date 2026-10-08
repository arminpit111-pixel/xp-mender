package de.xpmender;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xpmender.json");
    private static ModConfig instance = new ModConfig();

    public boolean enabled = true;
    public int hudX = 8;
    public int hudY = 8;
    /** false = absolute fehlende Haltbarkeit angleichen, true = Prozent angleichen */
    public boolean percentMode = false;
    public boolean includeMainHand = false;
    public boolean includeOffHand = false;
    public boolean showBackground = true;
    public boolean showDurability = true;
    /** Erwartete XP pro Bottle (vanilla 3-11, Durchschnitt 7) */
    public int xpPerBottle = 7;

    public static ModConfig get() {
        return instance;
    }

    public static void load() {
        if (Files.exists(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) {
                    instance = loaded;
                }
            } catch (Exception e) {
                System.err.println("[XP Mender] Config konnte nicht geladen werden: " + e);
            }
        }
        instance.xpPerBottle = Math.max(3, Math.min(11, instance.xpPerBottle));
        save();
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(FILE)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            System.err.println("[XP Mender] Config konnte nicht gespeichert werden: " + e);
        }
    }

    public void resetHudPosition() {
        hudX = 8;
        hudY = 8;
    }
}
