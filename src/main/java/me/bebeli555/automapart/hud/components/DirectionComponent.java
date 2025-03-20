package me.bebeli555.automapart.hud.components;

import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.math.Direction;

public class DirectionComponent extends HudComponent {
	public static Setting direction = new Setting(Mode.BOOLEAN, "Direction", true, "Shows the direction u are looking at");

	public DirectionComponent() {
		super(HudCorner.BOTTOM_LEFT, direction);
	}
	
	@Override
 	public void onRender(DrawContext context, float partialTicks) {
		int amount = 0;
		if (corner == HudCorner.BOTTOM_LEFT || corner == HudCorner.BOTTOM_RIGHT) {
			amount = 10;
			if (mc.currentScreen instanceof ChatScreen) {
				amount += 14;
			}
		}

		Direction dir = mc.cameraEntity.getMovementDirection();
		String text = w + "North " + g + "[" + w + "-Z" + g + "]";
		
		if (dir == Direction.EAST) {
			text = w + "East " + g + "[" + w + "+X" + g + "]";
		} else if (dir == Direction.SOUTH) {
			text = w + "South " + g + "[" + w + "+Z" + g + "]";
		} else if (dir == Direction.WEST) {
			text = w + "West " + g + "[" + w + "-X" + g + "]";
		}
		
		drawString(context, text, 0, -amount, -1);
	}
}
