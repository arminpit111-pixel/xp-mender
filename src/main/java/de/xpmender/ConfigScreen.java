package de.xpmender;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Supplier;

/**
 * Menue (Taste K): alle Einstellungen + HUD per Maus verschieben.
 */
public class ConfigScreen extends Screen {

    private final ModConfig cfg = ModConfig.get();

    private boolean dragging = false;
    private int dragOffsetX;
    private int dragOffsetY;
    private int hudW = 0;
    private int hudH = 0;

    public ConfigScreen() {
        super(Text.literal("XP Mender"));
    }

    @Override
    protected void init() {
        int bx = this.width / 2 - 100;
        int y = this.height / 2 - 100;
        int step = 24;

        addToggle(bx, y, () -> Text.literal("Mod: " + onOff(cfg.enabled)), () -> cfg.enabled = !cfg.enabled);
        y += step;
        addToggle(bx, y,
                () -> Text.literal("Angleichen nach: " + (cfg.percentMode ? "Prozent" : "Haltbarkeit")),
                () -> cfg.percentMode = !cfg.percentMode);
        y += step;
        addToggle(bx, y, () -> Text.literal("Haupthand einbeziehen: " + onOff(cfg.includeMainHand)),
                () -> cfg.includeMainHand = !cfg.includeMainHand);
        y += step;
        addToggle(bx, y, () -> Text.literal("Offhand einbeziehen: " + onOff(cfg.includeOffHand)),
                () -> cfg.includeOffHand = !cfg.includeOffHand);
        y += step;
        addToggle(bx, y, () -> Text.literal("Haltbarkeit anzeigen: " + onOff(cfg.showDurability)),
                () -> cfg.showDurability = !cfg.showDurability);
        y += step;
        addToggle(bx, y, () -> Text.literal("Hintergrund: " + onOff(cfg.showBackground)),
                () -> cfg.showBackground = !cfg.showBackground);
        y += step;

        // XP pro Bottle: - [Wert] +
        ButtonWidget xpLabel = addDrawableChild(ButtonWidget.builder(
                Text.literal("XP pro Bottle: " + cfg.xpPerBottle), b -> {}).dimensions(bx + 24, y, 152, 20).build());
        xpLabel.active = false;
        addDrawableChild(ButtonWidget.builder(Text.literal("-"), b -> {
            cfg.xpPerBottle = Math.max(3, cfg.xpPerBottle - 1);
            xpLabel.setMessage(Text.literal("XP pro Bottle: " + cfg.xpPerBottle));
        }).dimensions(bx, y, 20, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> {
            cfg.xpPerBottle = Math.min(11, cfg.xpPerBottle + 1);
            xpLabel.setMessage(Text.literal("XP pro Bottle: " + cfg.xpPerBottle));
        }).dimensions(bx + 180, y, 20, 20).build());
        y += step;

        addDrawableChild(ButtonWidget.builder(Text.literal("HUD zurücksetzen"), b -> cfg.resetHudPosition())
                .dimensions(bx, y, 200, 20).build());
        y += step;
        addDrawableChild(ButtonWidget.builder(Text.literal("Fertig"), b -> close())
                .dimensions(bx, y, 200, 20).build());
    }

    private void addToggle(int x, int y, Supplier<Text> label, Runnable action) {
        addDrawableChild(ButtonWidget.builder(label.get(), b -> {
            action.run();
            b.setMessage(label.get());
        }).dimensions(x, y, 200, 20).build());
    }

    private static String onOff(boolean value) {
        return value ? "AN" : "AUS";
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("XP Mender"), this.width / 2, 10, 0xFFFFFFFF);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("HUD mit der Maus an die gewünschte Stelle ziehen"), this.width / 2, 24, 0xFFAAAAAA);

        // HUD-Vorschau (immer sichtbar, auch ohne Bottles)
        int[] size = HudRenderer.render(ctx, true);
        hudW = size[0];
        hudH = size[1];

        if (hudW > 0) {
            int p = HudRenderer.padding();
            int x0 = cfg.hudX - p;
            int y0 = cfg.hudY - p;
            int x1 = cfg.hudX + hudW + p;
            int y1 = cfg.hudY + hudH + p;
            int color = (dragging || isOverHud(mouseX, mouseY)) ? 0xFFFFFF55 : 0xFF888888;
            ctx.fill(x0 - 1, y0 - 1, x1 + 1, y0, color);
            ctx.fill(x0 - 1, y1, x1 + 1, y1 + 1, color);
            ctx.fill(x0 - 1, y0, x0, y1, color);
            ctx.fill(x1, y0, x1 + 1, y1, color);
        }
    }

    private boolean isOverHud(double mx, double my) {
        if (hudW <= 0) {
            return false;
        }
        int p = HudRenderer.padding();
        return mx >= cfg.hudX - p && mx <= cfg.hudX + hudW + p
                && my >= cfg.hudY - p && my <= cfg.hudY + hudH + p;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) {
            return true;
        }
        if (click.button() == 0 && isOverHud(click.x(), click.y())) {
            dragging = true;
            dragOffsetX = (int) click.x() - cfg.hudX;
            dragOffsetY = (int) click.y() - cfg.hudY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (dragging) {
            int p = HudRenderer.padding();
            cfg.hudX = clamp((int) click.x() - dragOffsetX, p, this.width - hudW - p);
            cfg.hudY = clamp((int) click.y() - dragOffsetY, p, this.height - hudH - p);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging) {
            dragging = false;
            ModConfig.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void removed() {
        ModConfig.save();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(Math.max(min, max), v));
    }
}
