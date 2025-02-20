package me.bebeli555.automapart.utils.globalsettings;

import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingList;
import me.bebeli555.automapart.settings.SettingValue;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.BooleanSupplier;

public class GlobalBorderSettings {
    public static boolean translateToTopOnce;

    public static SettingList get(Setting parent, boolean enabled, boolean rainbow, int color, float size, BooleanSupplier visible) {
        SettingList settingList = new SettingList();

        Setting border = new Setting(parent, Mode.BOOLEAN, "Border", enabled, "Border settings");
            Setting borderRainbow = new Setting(border, Mode.BOOLEAN, "Rainbow", rainbow, "Use rainbow effect");
            Setting borderColor = new Setting(border, Mode.COLOR, "Color", color, "Color if rainbow is off");
            Setting borderSize = new Setting(border, Mode.DOUBLE, "Size", new SettingValue(size, 0.1, 5, 0.1), "Border size");

        settingList.list.add(border);
        settingList.list.add(borderRainbow);
        settingList.list.add(borderColor);
        settingList.list.add(borderSize);

        return settingList;
    }

    public static SettingList get(Setting parent, boolean enabled, boolean rainbow, int color, float size) {
        return get(parent, enabled, rainbow, color, size, () -> true);
    }

    public static SettingList get(Setting parent) {
        return get(parent, false, true, -1);
    }

    public static SettingList get(Setting parent, boolean enabled, boolean rainbow, int color) {
        return get(parent, enabled, rainbow, color, 0.3f, () -> true);
    }

    /**
     * Render the border with the generated setting list
     */
    public static void render(SettingList list, MatrixStack stack, double x, double y, double x2, double y2) {
        if (!list.list.get(0).bool()) {
            return;
        }

        int color = list.list.get(1).bool() ? HudComponent.getRainbow() : list.list.get(2).asInt();
        if (list.list.get(1).bool()) {
            HudComponent.factorRainbow();
        }

        render(stack, x + 0.5, y + 0.5, x2 - 0.3, y2, color, list.list.get(3).asDouble());
    }

    public static void renderWithBackground(SettingList list, MatrixStack stack, double x, double y, double x2, double y2, int background) {
        Gui.drawRect(stack, x, y, x2, y2, background);
        render(list, stack, x, y, x2, y2);
    }

    public static void render(MatrixStack stack, double x, double y, double x2, double y2, int color, double size) {
        Gui.drawLine(stack, x - size, y - size, x2 + size, y, color, translateToTopOnce); //Top
        Gui.drawLine(stack, x - size, y2, x2 + size, y2 + size, color, translateToTopOnce); //Bottom

        Gui.drawLine(stack, x - size, y, x, y2, color, translateToTopOnce); //Left
        Gui.drawLine(stack, x2, y, x2 + size, y2, color, translateToTopOnce); //Right

        translateToTopOnce = false;
    }
}
