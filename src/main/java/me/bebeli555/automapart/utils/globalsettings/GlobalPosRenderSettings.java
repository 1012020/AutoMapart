package me.bebeli555.automapart.utils.globalsettings;

import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingList;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.utils.render3d.Mesh;
import me.bebeli555.automapart.utils.render3d.Renderer3D;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.awt.*;
import java.util.List;
import java.util.function.BooleanSupplier;

public class GlobalPosRenderSettings {
    public static final Color EMPTY_COLOR = new Color(0, 0, 0, 0);

    public static SettingList get(boolean booleanSetting, String labelName, String description, Setting parent, String defaultMode, Color defaultColor, Color defaultFillColor, float defaultLineWidth, List<Setting.ModeListType> modes, BooleanSupplier isVisible) {
        Setting label;
        if (parent == null) {
            label = new Setting(booleanSetting ? Mode.BOOLEAN : Mode.LABEL, labelName, true, isVisible, description);
        } else {
            label = new Setting(parent, booleanSetting ? Mode.BOOLEAN : Mode.LABEL, labelName, true, isVisible, description);
        }

        Setting mode = new Setting(label, "Mode", defaultMode, modes);
        Setting color = new Setting(label, Mode.COLOR, "Color", defaultColor.getRGB(), "Color for the rendering");
        Setting fillColor = new Setting(label, Mode.COLOR, "FillColor", defaultFillColor.getRGB(), (() -> !mode.string().toLowerCase().contains("filled")), "Will also apply a fill with this color", "By default the alpha of this is 0 so it wont render", "So you could render an outline lines then a low transparency fill");
        Setting lineWidth = new Setting(label, Mode.DOUBLE, "LineWidth", new SettingValue(defaultLineWidth, 1, 10, 0.05), (() -> mode.string().toLowerCase().contains("line")), "Size of the rendered lines");
        Setting depth = new Setting(label, Mode.BOOLEAN, "Depth", false, "Allows you to see it through blocks and everything else, IF OFF");

        SettingList list = new SettingList();
        list.add(label, mode, color, fillColor, lineWidth, depth);

        return list;
    }

    public static SettingList get(boolean booleanSetting, String labelName, String description, Setting parent, String defaultMode, Color defaultColor, Color defaultFillColor, float defaultLineWidth) {
        return get(booleanSetting, labelName, description, parent, defaultMode, defaultColor, defaultFillColor, defaultLineWidth, RenderMode.getAll(), () -> true);
    }

    public static SettingList get(boolean booleanSetting, String labelName, String description, Setting parent, String defaultMode, Color defaultColor, float defaultLineWidth) {
        return get(booleanSetting, labelName, description, parent, defaultMode, defaultColor, EMPTY_COLOR, defaultLineWidth);
    }

    public static SettingList get(Setting parent, String defaultMode, Color defaultColor, float defaultLineWidth) {
        return get(false, "RenderType", "Settings about how the position is rendered in 3D", parent, defaultMode, defaultColor, EMPTY_COLOR, defaultLineWidth);
    }

    public static void render(SettingList settingList, Renderer3D renderer, Object pos, Color customColor) {
        render(settingList, renderer, pos, customColor, null);
    }

    public static void render(SettingList settingList, Renderer3D renderer, Object pos, Color customColor, Color customFillColor) {
        String mode = settingList.get(1).string();
        Color color = new Color(settingList.get(2).asInt(), true);
        Color fillColor = new Color(settingList.get(3).asInt(), true);

        if (customColor != null) {
            color = customColor;
        }

        if (customFillColor != null) {
            fillColor = customFillColor;
        }

        Mesh.depth = settingList.get(5).bool();
        if (mode.equals(RenderMode.SIDE_OUTLINE.toString())) {
            if (pos instanceof BlockPos blockPos) {
                if (fillColor.getAlpha() > 0) renderer.fillTop(blockPos, fillColor);
                renderer.linesAroundTop(blockPos, settingList.get(4).asFloat(), color);
            } else if (pos instanceof Box box) {
                if (fillColor.getAlpha() > 0) renderer.fill(box, fillColor);
                renderer.boxLines(box, settingList.get(4).asFloat(), color);
            }
        } else if (mode.equals(RenderMode.BOX_LINE.toString())) {
            if (pos instanceof BlockPos blockPos) {
                if (fillColor.getAlpha() > 0) renderer.fill(blockPos, fillColor);
                renderer.boxLines(blockPos, settingList.get(4).asFloat(), color);
            } else if (pos instanceof Box box) {
                if (fillColor.getAlpha() > 0) renderer.fill(box, fillColor);
                renderer.boxLines(box, settingList.get(4).asFloat(), color);
            }
        } else if (mode.equals(RenderMode.FILLED_SIDE.toString())) {
            if (pos instanceof BlockPos blockPos) {
                renderer.fillTop(blockPos, color);
            } else if (pos instanceof Box box) {
                renderer.fill(box, color);
            }
        } else if (mode.equals(RenderMode.FILLED_BOX.toString())) {
            if (pos instanceof BlockPos blockPos) {
                renderer.fill(blockPos, color);
            } else if (pos instanceof Box box) {
                renderer.fill(box, color);
            }
        }
    }

    public static void render(SettingList settingList, Renderer3D renderer, Object pos) {
        render(settingList, renderer, pos, null);
    }

    public static boolean isMainToggled(SettingList list) {
        return list.get(0).bool();
    }

    public static class RenderMode {
        public static Setting.ModeListType SIDE_OUTLINE = new Setting.ModeListType("SideOutline", "Renders a line outline at the top or the side");
        public static Setting.ModeListType BOX_LINE = new Setting.ModeListType("BoxLine", "Renders lines at all edges of the position like a box");
        public static Setting.ModeListType FILLED_BOX = new Setting.ModeListType("FilledBox", "Fills the position completely");
        public static Setting.ModeListType FILLED_SIDE = new Setting.ModeListType("FilledSide", "Renders a filled rectangle at the top or the side");

        public static List<Setting.ModeListType> getAll() {
            return List.of(new Setting.ModeListType[]{SIDE_OUTLINE, BOX_LINE, FILLED_BOX, FILLED_SIDE});
        }

        public static List<Setting.ModeListType> getBoxOnly() {
            return List.of(new Setting.ModeListType[]{BOX_LINE, FILLED_BOX});
        }
    }
}
