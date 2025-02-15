package me.bebeli555.automapart.hud.components;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingList;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.utils.InventoryUtils;
import me.bebeli555.automapart.utils.RenderUtils2D;
import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class InventoryViewerComponent extends HudComponent {
    public static Setting inventoryViewer = new Setting(Mode.BOOLEAN, "InventoryViewer", false, "Shows ur inventory contents");
        public static SettingList border = GlobalBorderSettings.get(inventoryViewer);
        public static Setting background = new Setting(inventoryViewer, Mode.COLOR, "Background", 2013265920, "Background color of the box");
        public static Setting scaleSetting = new Setting(inventoryViewer, Mode.DOUBLE, "Scale", new SettingValue(0.8, 0.25, 3, 0.05), "Scale of the box");

    public InventoryViewerComponent() {
        super(HudCorner.TOP_LEFT, inventoryViewer);
        this.defaultX = 2;
        this.defaultY = 190;
    }

    @Override
    public void onRender(DrawContext context, float partialTicks) {
        MatrixStack stack = context.getMatrices();

        float scale = scaleSetting.asFloat();
        int width = 155;
        int height = 53;
        int gap = 17;
        int x = (int)(getxAdd() / scale);
        int y = (int)(getyAdd() / scale);

        stack.push();
        stack.scale(scale, scale, scale);

        //Render box and border
        Gui.drawRect(stack, x, y, x + width, y + height, background.asInt());
        GlobalBorderSettings.render(border, stack, x, y, x + width, y + height);

        //Render items
        int renderX = x;
        int renderY = y + 1;
        int rendered = 0;

        for (InventoryUtils.StackAndSlot stackAndSlot : InventoryUtils.getStacksWithSlots()) {
            if (stackAndSlot.slot() < 9) {
                continue;
            }

            RenderUtils2D.renderItemInGui(context.getMatrices(), stackAndSlot.itemStack(), renderX, renderY);
            RenderUtils2D.renderGuiItemOverlay(context, mc.textRenderer, stackAndSlot.itemStack(), renderX, renderY, "" + stackAndSlot.itemStack().getCount());

            rendered++;
            renderX += gap;
            if (rendered == 9) {
                renderX = x;
                renderY += gap;
                rendered = 0;
            }
        }

        RenderSystem.disableCull();
        stack.pop();
        this.renderedPoints.add(new HudPoint(x * scale, y * scale, (x + width) * scale, (y + height) * scale));
    }
}
