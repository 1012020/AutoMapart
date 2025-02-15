package me.bebeli555.automapart.hud.components;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

public class WatermarkComponent extends HudComponent {
	public static Setting waterMark = new Setting(Mode.BOOLEAN, "Watermark", true, "Shows watermark");

	public WatermarkComponent() {
		super(HudCorner.TOP_LEFT, waterMark);
	}

	@Override
	public void onRender(DrawContext context, float partialTicks) {
		drawString(context, Formatting.BLUE + Mod.NAME + w + " v" + Mod.VERSION, 0, 0, -1);
	}
}
