package me.bebeli555.automapart.hud.components;

import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.gui.windows.windows.pickers.pickers.ItemPicker;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.utils.RenderUtils2D;
import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingList;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.utils.InventoryUtils;
import me.bebeli555.automapart.utils.Utils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemCountsComponent extends HudComponent {
    public static Setting itemCounts = new Setting(Mode.BOOLEAN, "ItemCounts", false, "Shows combat item counts");
        public static Setting itemPicker = new Setting(itemCounts, Mode.PICKER, "ItemPicker", "Obsidian$End Crystal$Enchanted Golden Apple$Totem of Undying$Bottle o' Enchanting", "Select the rendered items");
        public static Setting scaleSetting = new Setting(itemCounts, Mode.DOUBLE, "Scale", new SettingValue(1, 0.1, 3, 0.025), "Scale of items");
        public static Setting gapSetting = new Setting(itemCounts, Mode.INTEGER, "Gap", new SettingValue(20, 5, 30, 1), "X gap");
        public static Setting background = new Setting(itemCounts, Mode.COLOR, "Background", 838860800, "Background color");
        public static SettingList border = GlobalBorderSettings.get(itemCounts);

    public ItemCountsComponent() {
        super(HudCorner.TOP_LEFT, itemCounts);
        this.defaultX = 3;
        this.defaultY = 144;
    }

    @Override
    public void onRender(DrawContext context, float partialTicks) {
        MatrixStack stack = context.getMatrices();

        List<ItemStack> list = new ArrayList<>();
        for (PickerWindow.PickerItem pickerItem : ItemPicker.INSTANCE.getSelectedFromSetting(itemPicker)) {
            Item item = Utils.getItemFromName(pickerItem.name);
            list.add(new ItemStack(item, InventoryUtils.getAmountOfItem(item)));
        }

        int gap = gapSetting.asInt();
        float scale = scaleSetting.asFloat();
        stack.push();
        stack.scale(scale, scale, scale);

        int x = (int)(getxAdd() / scale);
        int y = (int)(getyAdd() / scale);

        //Render background
        HudPoint point = new HudPoint(getxAdd(), getyAdd() - 1, getxAdd() + list.size() * (double)gap * scale, getyAdd() + (18 * scale));
        this.renderedPoints.add(point);
        Gui.drawRect(stack, point.x / scale, point.y / scale, point.x2 / scale, point.y2 / scale, background.asInt());

        //Render border
        GlobalBorderSettings.render(border, stack, point.x / scale, point.y / scale, point.x2 / scale, point.y2 / scale);

        for (int i = 0; i < list.size(); i++) {
            int realCount = list.get(i).getCount();
            int renderX = x + i * gap;
            if (list.get(i).getCount() <= 0) {
                list.get(i).setCount(1);
            }

            RenderUtils2D.renderItemInGui(context.getMatrices(), list.get(i), renderX, y);
            RenderUtils2D.renderGuiItemOverlay(context, mc.textRenderer, list.get(i), renderX, y, "" + realCount);
        }

        stack.pop();
    }
}
