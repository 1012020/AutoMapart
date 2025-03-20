package me.bebeli555.automapart.hud.components;

import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.world.dimension.DimensionTypes;

public class CoordsComponent extends HudComponent {
	public static Setting coords = new Setting(Mode.BOOLEAN, "Coords", true, "Shows ur coords");
		public static Setting netherCoords = new Setting(coords, Mode.BOOLEAN, "NetherCoords", true, "Also renders nether coords", "Or overworld if ur in nether");

	public CoordsComponent() {
		super(HudCorner.BOTTOM_LEFT, coords);
	}
	
	@Override
 	public void onRender(DrawContext context, float partialTicks) {
		float amount = 0;
		if (mc.currentScreen instanceof ChatScreen) {
			amount = 14;
		}
		
		String text = g + "XYZ " + w + decimal(mc.cameraEntity.getPos().x, 1) + g + ", " + w + decimal(mc.cameraEntity.getPos().y, 1) + g + ", " + w + decimal(mc.cameraEntity.getPos().z, 1) + " ";
		if (netherCoords.bool()) {
			if (mc.player.getWorld().getDimensionKey() != DimensionTypes.THE_NETHER) {
				text += g + "[" + w + decimal(mc.cameraEntity.getPos().x / 8, 1) + g + ", " + w + decimal(mc.cameraEntity.getPos().z / 8, 1) + g + "]";
			} else {
				text += g + "[" + w + decimal(mc.cameraEntity.getPos().x * 8, 1) + g + ", " + w + decimal(mc.cameraEntity.getPos().z * 8, 1) + g + "]";
			}
		}
		
		drawString(context, text, 0, -amount, -1);
	}
}
