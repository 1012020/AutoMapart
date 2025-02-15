package me.bebeli555.automapart.hud.components;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.hud.HudEditor;
import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingList;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.utils.EntityUtils;
import me.bebeli555.automapart.utils.PlayerUtils;
import me.bebeli555.automapart.utils.font.ColorHolder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class TextRadarComponent extends HudComponent {
    public static Setting textRadar = new Setting(Mode.BOOLEAN, "TextRadar", false, "Shows names and distances of players");
        public static Setting scaleSetting = new Setting(textRadar, Mode.DOUBLE, "Scale", new SettingValue(1, 0.3, 3, 0.1), "Scale for the whole thing");
        public static Setting nameColor = new Setting(textRadar, Mode.COLOR, "NameColor", -1713177, "Color of the player name");
        public static Setting background = new Setting(textRadar, Mode.COLOR, "Background", 838860800, "Background color");
        public static SettingList border = GlobalBorderSettings.get(textRadar, false, true, -16777216);
        public static Setting gapSetting = new Setting(textRadar, Mode.INTEGER, "Gap", new SettingValue(8, 1, 35, 1), "Gap between the names");
        public static Setting maxEntries = new Setting(textRadar, Mode.INTEGER, "MaxEntries", new SettingValue(10, 2, 100, 1), "Max names to render");

    public TextRadarComponent() {
        super(HudCorner.TOP_LEFT, textRadar);
        this.defaultX = 244;
        this.defaultY = 48;
    }

    @Override
    public void onRender(DrawContext context, float partialTicks) {
        MatrixStack stack = context.getMatrices();

        //Render
        int i = 0;
        float scale = scaleSetting.asFloat();
        int gap = gapSetting.asInt();
        float x = (float)((getxAdd() + add(scale)) / scale);
        float y = (float)((getyAdd() + add(scale)) / scale);
        int highestWidth = 0;

        stack.push();
        RenderSystem.disableBlend();
        stack.scale(scale, scale, scale);

        List<String> list = new ArrayList<>();
        for (Entity entity : EntityUtils.getAll()) {
            if (entity instanceof PlayerEntity player && !entity.equals(mc.player)) {
                String name = entity.getName().getString();
                double d = mc.player.distanceTo(entity);
                String distance = decimal(d, 1) + "m";

                list.add(new ColorHolder(nameColor.asInt()) + PlayerUtils.getPlayerColor(player) + name + getDistanceColor(d) + " " + distance);
            }
        }

        if (list.isEmpty() && HudEditor.INSTANCE.isOn()) {
            list.add(new ColorHolder(nameColor.asInt()) + "ExamplePlayerEntity " + Formatting.GREEN + "31.2m");
        }

        for (String input : list) {
            int size = Gui.fontRenderer.getWidth(stack, input);
            if (size > highestWidth) {
                highestWidth = size;
            }

            Gui.fontRenderer.drawString(stack, input, x, y + i * gap, -1);
            i++;

            if (i > maxEntries.asInt()) {
                int more = list.size() - i;
                if (more > 0) {
                    size = Gui.fontRenderer.getWidth(stack, "and " + more + " more...");
                    if (size > highestWidth) {
                        highestWidth = size;
                    }

                    Gui.fontRenderer.drawString(stack, new ColorHolder(nameColor.asInt()) + "and " + more + " more...", x, y + i * gap, -1);
                    i++;
                    break;
                }
            }
        }

        if (i != 0) {
            HudPoint point = new HudPoint(getxAdd(), getyAdd(), getxAdd() + (highestWidth * scale) + add(scale) * 2, getyAdd() + ((i * gap) * scale) + add(scale));
            this.renderedPoints.add(point);
            point = new HudPoint(point.x / scale, point.y / scale, point.x2 / scale, point.y2 / scale);

            //Render background
            Gui.drawRect(stack, point.x, point.y, point.x2, point.y2, background.asInt());

            //Render border
            GlobalBorderSettings.render(border, stack, point.x, point.y, point.x2, point.y2);
        }

        RenderSystem.enableBlend();
        stack.pop();
    }

    public static Formatting getDistanceColor(double distance) {
        if (distance < 11) {
            return Formatting.RED;
        } else if (distance < 30) {
            return Formatting.YELLOW;
        } else {
            return Formatting.GREEN;
        }
    }
}
