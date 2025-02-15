package me.bebeli555.automapart.hud;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.components.ArrayListComponent;
import me.bebeli555.automapart.hud.components.DevPanelComponent;
import me.bebeli555.automapart.hud.components.InfoClusterComponent;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.utils.RainbowUtil;
import me.bebeli555.automapart.utils.font.ColorHolder;
import me.bebeli555.automapart.utils.font.SierraFontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HudComponent extends Mod {
	public static ArrayList<HudComponent> components = new ArrayList<HudComponent>();
	public HudCorner corner;
	private double xAdd;
	private double yAdd;
	public double defaultX, defaultY;
	public boolean setFromSettings;
	public String name;
	public Setting mainSetting;
	public long lastNoRender;
	public HudPoint draggingPoint;
	public ArrayList<HudPoint> renderedPoints = new ArrayList<HudPoint>();
	public HashMap<String, Double[]> textAdds = new HashMap<>();
	public List<String> fetchedTextAdds = new ArrayList<>();

	public static Formatting w = Formatting.WHITE;
	public static Formatting g = Formatting.GRAY;
	public static double renderedScale;
	public static RainbowUtil rainbow = new RainbowUtil();

	public HudComponent(HudCorner defaultCorner, Setting mainSetting) {
		try {
			mainSetting.bool();
		} catch (ClassCastException e) {
			System.err.println("SEVERE ERROR, hud component " + name + " mainsetting must be boolean");
			e.printStackTrace();
			return;
		}

		this.corner = defaultCorner;
		this.mainSetting = mainSetting;
		this.name = mainSetting.name;
		components.add(this);
	}
	
	public void onRender(DrawContext context, float partialTicks) {}

	public double getScale() {return 1;}

	public void drawString(DrawContext context, String text, float x, float y, int color) {
		drawString(context, text, x, y, color, 1);
	}

	public void drawString(DrawContext context, String text, float x, float y, int color, double scale) {
		int displayWidth = (int)((mc.getWindow().getWidth() / scale) / ClientSettings.hudScale.asFloat());
		int displayHeight = (int)((mc.getWindow().getHeight() / scale) / ClientSettings.hudScale.asFloat());

		if (corner == HudCorner.BOTTOM_RIGHT) {
			drawString2(context, text, displayWidth - 1 - Gui.fontRenderer.getWidth(context.getMatrices(), text) + x, (displayHeight - 9) + y, color, scale);
		} else if (corner == HudCorner.BOTTOM_LEFT) {
			drawString2(context, text, x + 1, (displayHeight - 9) + y, color, scale);
		} else if (corner == HudCorner.TOP_LEFT) {
			drawString2(context, text, x + 1, y + 1, color, scale);
		} else if (corner == HudCorner.TOP_RIGHT) {
			drawString2(context, text, displayWidth - Gui.fontRenderer.getWidth(context.getMatrices(), text) - 1, y + 1, color, scale);
		} else if (corner == HudCorner.NONE) {
			drawString2(context, text, x, y, color, scale);
		}
	}
	
	private void drawString2(DrawContext context, String text, float x, float y, int color, double scale) {
		MatrixStack stack = context.getMatrices();
		x += getxAdd() / scale;
		y += getyAdd() / scale;

		if (this instanceof InfoClusterComponent) {
			String getName = text.split(" ")[0].split(ColorHolder.endChar)[1];
			fetchedTextAdds.add(getName);
			Double[] value = textAdds.get(getName);

			if (value != null) {
				x += value[0];
				y += value[1];
			}
		}

		HudPoint point = new HudPoint(
				(x - add()) * scale,
				(y - add() + 2) * scale,
				(x + Gui.fontRenderer.getWidth(stack, text) + add()) * scale,
				(y + Gui.fontRenderer.getHeight(stack) + add() - 4) * scale
		);

		point.drawnString = true;
		if (this instanceof InfoClusterComponent) {
			point.name = text.split(" ")[0].split(ColorHolder.endChar)[1];
			Gui.drawRect(stack, point.x / scale, point.y / scale, point.x2 / scale, point.y2 / scale, InfoClusterComponent.background.asInt());
		} else if (this instanceof ArrayListComponent) {
			Gui.drawRect(stack, point.x / scale, point.y / scale, point.x2 / scale, point.y2 / scale, ArrayListComponent.background.asInt());
		}

		renderedPoints.add(point);
		Gui.fontRenderer.drawString(stack, text, x, y, color);
	}

	public static int getLongestString(List<String> list, double scale) {
		int longest = 0;
		for (String s : list) {
			int width = (int)(Gui.fontRenderer.getWidth(new MatrixStack(), s) * scale) + 3;
			if (width > longest) {
				longest = width;
			}
		}

		return longest;
	}

	public boolean shouldRender() {
		return mainSetting.bool();
	}

	//Turn double into one decimal string
	public static String decimal(double d, int decimal) {
		String s = Double.toString(d);
		try {
			s = s.substring(0, s.indexOf(".") + 1 + decimal);
		} catch (IndexOutOfBoundsException e) {
			s = s.substring(0, s.indexOf(".") + 1);
		}

		if (s.endsWith(".")) {
			s = s.substring(0, s.length() - 1);
		}

		return s;
	}

	public static int getRainbow() {
		return rainbow.getRainbowColorAt(rainbow.factor);
	}

	public static void factorRainbow() {
		rainbow.factor += ClientSettings.hudRainbowFactor.asInt();
		if (rainbow.factor >= 355) {
			rainbow.factor = 0;
		}

		DevPanelComponent.put("RainbowFactor", "" + rainbow.factor);
	}

	public float add() {
		return 3;
	}

	public float add(double scale) {
		return 3 * (float)scale;
	}

	public static void setDefaults() {
		//Set HUD default values
		for (HudComponent component : HudComponent.components) {
			if (!component.setFromSettings) {
				component.setxAdd(component.defaultX);
				component.setyAdd(component.defaultY);
			}
		}
	}

	public static float currentScale() {
		float scale = (float)1 / SierraFontRenderer.getMcScale();
		scale *= ClientSettings.hudScale.asFloat();

		return scale;
	}

	public static float defaultScale() {
		float scale = (float)1 / SierraFontRenderer.getMcScale();
		scale *= (double)ClientSettings.hudScale.defaultValue;

		return scale;
	}

	public double getxAdd() {
		return xAdd * (defaultScale() / currentScale());
	}

	public double getxAddNoScale() {
		return xAdd;
	}

	public void setxAdd(double xAdd) {
		this.xAdd = xAdd;
	}

	public double getyAdd() {
		return yAdd * (defaultScale() / currentScale());
	}

	public double getyAddNoScale() {
		return yAdd;
	}

	public void setyAdd(double yAdd) {
		this.yAdd = yAdd;
	}

	public enum HudCorner {
		TOP_RIGHT(0),
		TOP_LEFT(1),
		BOTTOM_RIGHT(2),
		BOTTOM_LEFT(3),
		NONE(4);
		
		public int id;
		HudCorner(int id) {
			this.id = id;
		}
		
		public static HudCorner getCornerFromId(int id) {
			for (HudCorner corner : HudCorner.values()) {
				if (corner.id == id) {
					return corner;
				}
			}
			
			return null;
		}
	}
	
	public class HudPoint {
		public double x, y, x2, y2;
		public String name;
		public boolean drawnString;
		public MatrixStack customStack;

		public HudPoint(double x, double y, double x2, double y2) {
			this.x = x;
			this.y = y;
			this.x2 = x2;
			this.y2 = y2;
		}

		public HudPoint(MatrixStack stack, double x, double y, double x2, double y2) {
			this(x, y, x2, y2);
			this.customStack = stack;
		}
	}
}
