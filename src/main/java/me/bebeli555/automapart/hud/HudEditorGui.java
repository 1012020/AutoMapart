package me.bebeli555.automapart.hud;

import me.bebeli555.automapart.Mod;
import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.events.game.ClientTickEvent;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.HudComponent.HudCorner;
import me.bebeli555.automapart.hud.HudComponent.HudPoint;
import me.bebeli555.automapart.hud.components.InfoClusterComponent;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.utils.input.Mouse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.Comparator;

public class HudEditorGui extends Screen {
	public static MinecraftClient mc = Mod.mc;
	public static int lastMouseX, lastMouseY;
	public static HudComponent dragging;

	public HudEditorGui() {
		super(Text.literal(Mod.NAME + "-HudEditor"));
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int realMouseX = mouseX;
		int realMouseY = mouseY;
		MatrixStack stack = context.getMatrices();

		//Sort components so recently enable is at the top
		HudComponent.components.sort(Comparator.comparingLong(c -> c.lastNoRender));

		//Rainbow
		HudComponent.rainbow.factor = 0;
		HudComponent.rainbow.setSpeed(ClientSettings.hudRainbowSpeed.asInt());
		HudComponent.rainbow.onUpdate();

		//Draw background
		Gui.drawRect(stack, 0, 0, 15000, 15000, HudEditor.backgroundColor.asInt());

		stack.push();
		float scale = HudComponent.currentScale();
		stack.scale(scale, scale, scale);

		mouseX = (int) (mouseX / scale);
		mouseY = (int) (mouseY / scale);

		//Render rectangle under the components
		if (HudEditor.color.bool()) {
			int extend = HudEditor.extend.asInt();
			for (HudComponent component : HudComponent.components) {
				if (component.shouldRender()) {

					component.renderedPoints.clear();
					component.onRender(context, delta);

					for (HudPoint point : component.renderedPoints) {
						Gui.drawRect(stack, (int) point.x - extend, (int) point.y - extend, (int) point.x2 + extend, (int) point.y2 + extend, HudEditor.colorColor.asInt());
					}
				}
			}
		}

		//Render components. Usually its done at Rendering class but its done here if the module is on
		//So the rectangles dont overlay the text
		for (HudComponent component : HudComponent.components) {
			if (component.shouldRender()) {
				component.renderedPoints.clear();
				component.onRender(context, delta);
			} else {
				component.setxAdd(component.defaultX);
				component.setyAdd(component.defaultY);
				component.lastNoRender = System.currentTimeMillis();
			}
		}

		//Drag components
		if (dragging != null) {
			if (Mouse.isButtonDown(0)) {
				double addX = (mouseX - lastMouseX) / dragging.getScale();
				double addY = (mouseY - lastMouseY) / dragging.getScale();

				if (dragging instanceof InfoClusterComponent && InfoClusterComponent.freeMove.bool()) {
					Double[] value = dragging.textAdds.get(dragging.draggingPoint.name);
					if (value != null) {
						value[0] += addX;
						value[1] += addY;
					} else {
						value = new Double[]{addX, addY};
					}

					dragging.textAdds.put(dragging.draggingPoint.name, value);
				} else {
					dragging.setxAdd(dragging.getxAddNoScale() + addX / (HudComponent.defaultScale() / HudComponent.currentScale()));
					dragging.setyAdd(dragging.getyAddNoScale() + addY / (HudComponent.defaultScale() / HudComponent.currentScale()));
				}
			} else {
				dragging = null;
			}
		}

		lastMouseX = mouseX;
		lastMouseY = mouseY;
		stack.pop();

		//Draw gui nodes
		Gui.gui.render(context, realMouseX, realMouseY, -9237487);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		Gui.gui.mouseClicked(x, y, button);

		HudComponent component = null;
		int extend = HudEditor.extend.asInt();
		for (HudComponent component2 : HudComponent.components) {
			if (component2.shouldRender()) {
				for (HudPoint point : component2.renderedPoints) {
					if (point.x2 + extend > lastMouseX && point.x - extend < lastMouseX && point.y2 + extend > lastMouseY && point.y - extend < lastMouseY) {
						component = component2;
						component.draggingPoint = point;
						break;
					}
				}
			}
		}

		if (component == null) {
			return true;
		}

		if (button == 0) {
			dragging = component;
		} else if (button == 1) {
			if (component.corner == HudCorner.BOTTOM_RIGHT) {
				component.corner = HudCorner.BOTTOM_LEFT;
			} else if (component.corner == HudCorner.BOTTOM_LEFT) {
				component.corner = HudCorner.TOP_LEFT;
			} else if (component.corner == HudCorner.TOP_LEFT) {
				component.corner = HudCorner.TOP_RIGHT;
			} else if (component.corner == HudCorner.TOP_RIGHT) {
				component.corner = HudCorner.BOTTOM_RIGHT;
			}

			component.setxAdd(0);
			component.setyAdd(0);
			component.lastNoRender = System.currentTimeMillis();
		} else if (button == 2) {
			component.setxAdd(component.defaultX);
			component.setyAdd(component.defaultY);
			component.lastNoRender = System.currentTimeMillis();
		}

		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return Gui.gui.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean charTyped(char key, int keyCode) {
		return Gui.gui.charTyped(key, keyCode);
	}

	@Subscribe
	public void tickEvent(ClientTickEvent e) {
		if (!(mc.currentScreen instanceof HudEditorGui)) {
			HudEditor.closedProgrammatically = true;
			HudEditor.INSTANCE.disable();
		}
	}
}
