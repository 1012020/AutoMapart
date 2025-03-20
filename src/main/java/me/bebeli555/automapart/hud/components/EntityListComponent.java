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
import me.bebeli555.automapart.utils.objects.Timer;
import me.bebeli555.automapart.utils.font.ColorHolder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

public class EntityListComponent extends HudComponent {
    public Timer timer = new Timer();
    public Map<String, Integer> hash = new HashMap<>();
    public List<NameAmount> list = new ArrayList<>();

    public static Setting entityList = new Setting(Mode.BOOLEAN, "EntityList", false, "Show names and amounts of entities");
        public static Setting scaleSetting = new Setting(entityList, Mode.DOUBLE, "Scale", new SettingValue(1, 0.3, 3, 0.1), "Scale for the whole thing");
        public static Setting nameColor = new Setting(entityList, Mode.COLOR, "NameColor", -1713177, "Color of the entity name");
        public static Setting amountColor = new Setting(entityList, Mode.COLOR, "AmountColor", -44664, "Color of the amount after name");
        public static Setting backgroundColor = new Setting(entityList, Mode.COLOR, "Background", 838860800, "Background color");
        public static SettingList border = GlobalBorderSettings.get(entityList, false, true, -16777216);
        public static Setting gapSetting = new Setting(entityList, Mode.INTEGER, "Gap", new SettingValue(8, 1, 35, 1), "Gap between the names");
        public static Setting maxEntries = new Setting(entityList, Mode.INTEGER, "MaxEntries", new SettingValue(10, 2, 100, 1), "Max names to render");
        public static Setting showItems = new Setting(entityList, Mode.BOOLEAN, "ShowItems", true, "Show grounditems");
        public static Setting sort = new Setting(entityList, Mode.BOOLEAN, "Sort", false, "Sorts the entities based on the amount to the top");

    public EntityListComponent() {
        super(HudCorner.TOP_LEFT, entityList);
        this.defaultX = 132;
        this.defaultY = 189;
    }

    @Override
    public void onRender(DrawContext context, float partialTicks) {
        MatrixStack stack = context.getMatrices();

        //Update hash
        if (timer.hasPassed(100)) {
            timer.reset();

            hash.clear();
            for (Entity entity : EntityUtils.getAll()) {
                if (!showItems.bool() && entity instanceof ItemEntity) {
                    continue;
                }

                if (!(entity instanceof PlayerEntity)) {
                    String name = entity.getName().getString();
                    Integer amount = hash.get(name);
                    if (amount == null) {
                        amount = 0;
                    }

                    if (entity instanceof ItemEntity) {
                        name = ((ItemEntity)entity).getStack().getItem().getName().getString();
                    }

                    hash.put(name, amount + 1);
                }
            }

            if (hash.isEmpty() && HudEditor.INSTANCE.isOn()) {
                hash.put("ExampleMob", 2);
            }

            //Create list
            List<NameAmount> tempList = new ArrayList<>();
            for (String key : hash.keySet()) {
                tempList.add(new NameAmount(key, hash.get(key)));
            }

            if (sort.bool()) {
                tempList.sort(Comparator.comparingInt(na -> na.amount));
                Collections.reverse(tempList);
            }

            list = tempList;
        }

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

        for (NameAmount nameAmount : list) {
            int size = Gui.fontRenderer.getWidth(stack, nameAmount.name + " " + nameAmount.amount);
            if (size > highestWidth) {
                highestWidth = size;
            }

            Gui.fontRenderer.drawString(stack, new ColorHolder(nameColor.asInt()) + nameAmount.name + new ColorHolder(amountColor.asInt()) + " " + nameAmount.amount, x, y + i * gap, -1);
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
            Gui.drawRect(stack, point.x, point.y, point.x2, point.y2, backgroundColor.asInt());

            //Render border
            GlobalBorderSettings.render(border, stack, point.x, point.y, point.x2, point.y2);
        }

        RenderSystem.enableBlend();
        stack.pop();
    }

    public static class NameAmount {
        public String name;
        public int amount;

        public NameAmount(String name, int amount) {
            this.name = name;
            this.amount = amount;
        }
    }
}
