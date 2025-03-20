package me.bebeli555.automapart.hud.components;

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
import net.minecraft.item.ItemStack;

import java.util.List;

public class ArmorComponent extends HudComponent {
	public static Setting armor = new Setting(Mode.BOOLEAN, "Armor" , false, "Shows wielded armor");
		public static Setting scale = new Setting(armor, Mode.DOUBLE, "Scale", new SettingValue(1, 0.1, 3, 0.025), "Scale for rendered items");
		public static Setting gap = new Setting(armor, Mode.INTEGER, "Gap", new SettingValue(16, 5, 30, 1), "X gap");
		public static Setting background = new Setting(armor, Mode.COLOR, "Background", 838860800, "Background color");
		public static SettingList border = GlobalBorderSettings.get(armor);

	public ArmorComponent() {
		super(HudCorner.NONE, armor);
		this.defaultX = 3;
		this.defaultY = 121;
	}

	@Override
	public void onRender(DrawContext context, float partialTicks) {
		MatrixStack stack = context.getMatrices();

		List<ItemStack> list = InventoryUtils.getStacks();
		ItemStack[] stacks = {
				InventoryUtils.getStack(39),
				InventoryUtils.getStack(38),
				InventoryUtils.getStack(37),
				InventoryUtils.getStack(36)
		};

		int gap = ArmorComponent.gap.asInt();
		float scale = ArmorComponent.scale.asFloat();
		stack.push();
		stack.scale(scale, scale, scale);

		int x = (int)(getxAdd() / scale);
		int y = (int)(getyAdd() / scale);

		//Render background
		HudPoint point = new HudPoint(getxAdd(), getyAdd() - 1, getxAdd() + 4 * (double)gap * scale, getyAdd() + (18 * scale));
		this.renderedPoints.add(point);
		Gui.drawRect(stack, point.x / scale, point.y / scale, point.x2 / scale, point.y2 / scale, background.asInt());

		//Render border
		GlobalBorderSettings.render(border, stack, point.x / scale, point.y / scale, point.x2 / scale, point.y2 / scale);

		for (int i = 0; i < stacks.length; i++) {
			int renderX = x + i * gap;
			final int finalI = i;
			int count = (int)list.stream().filter(s -> s.getItem().equals(stacks[finalI].getItem())).count();

			RenderUtils2D.renderItemInGui(context.getMatrices(), stacks[i], renderX, y);
			RenderUtils2D.renderGuiItemOverlay(context, mc.textRenderer, stacks[i], renderX, y, "" + (count != 0 ? count : ""));
		}

		stack.pop();
	}
}
