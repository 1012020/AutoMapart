package me.bebeli555.automapart.hud.components;

import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingList;
import me.bebeli555.automapart.settings.SettingValue;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Comparator;
import java.util.NavigableMap;
import java.util.TreeMap;

public class DevPanelComponent extends HudComponent {
    private static NavigableMap<String, String> map = new TreeMap<>();

    public static Setting devPanel = new Setting(Mode.BOOLEAN, "DevPanel", false, "Developer panel with some informations");
        public static Setting scaleSetting = new Setting(devPanel, Mode.DOUBLE, "Scale", new SettingValue(1, 0.1, 3, 0.025), "Scale of the panel");
        public static Setting gapSetting = new Setting(devPanel, Mode.INTEGER, "Gap", new SettingValue(8, 1, 35, 1), "Gap between the strings");
        public static Setting background = new Setting(devPanel, Mode.COLOR, "Background", 1660944384, "Background color");
        public static SettingList border = GlobalBorderSettings.get(devPanel);

    public DevPanelComponent() {
        super(HudCorner.TOP_LEFT, devPanel);
        this.defaultX = 241;
        this.defaultY = 138;
    }

    @Override
    public void onRender(DrawContext context, float partialTicks) {
        MatrixStack stack = context.getMatrices();

        float scale = scaleSetting.asFloat();
        stack.push();
        stack.scale(scale, scale, scale);

        String highestWidthString = map.entrySet().stream().max(Comparator.comparing(entry -> entry.getKey().length() + entry.getValue().length())).map(entry -> entry.getKey() + entry.getValue()).orElse("");
        int highestWidth = (int)(Gui.fontRenderer.getWidth(stack, highestWidthString) * scale);

        HudPoint point = new HudPoint(getxAdd() - add(scale), getyAdd() - add(scale), getxAdd() + highestWidth + add(scale) * 3, getyAdd() + ((map.size() - 1) * gapSetting.asInt()) * scale + (add(scale) * 3));
        this.renderedPoints.add(point);
        point = new HudPoint(point.x / scale, point.y / scale, point.x2 / scale, point.y2 / scale);

        //Render background
        Gui.drawRect(stack, point.x, point.y, point.x2, point.y2, background.asInt());

        //Render border
        GlobalBorderSettings.render(border, stack, point.x, point.y, point.x2, point.y2);

        //Render strings
        int i = 0;
        for (String key : map.keySet()) {
            Gui.fontRenderer.drawString(stack, g + key + ": " + w + map.get(key), (float) getxAdd() / scale, (float)(getyAdd() / scale) + (i * gapSetting.asInt()), -1);
            i++;
        }

        stack.pop();
    }

    public static void put(String key, Object value) {
        if (devPanel.bool()) {
            map.put(key, "" + value);
        }
    }
}
