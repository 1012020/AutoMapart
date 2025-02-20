package me.bebeli555.automapart.hud.components;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.utils.RainbowUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.*;

public class ArrayListComponent extends HudComponent {
	public static int lastArraylistSize;
	public static String lastSortMode;
	public static List<Mod> arraylist = new ArrayList<>();

	public static Setting arrayList = new Setting(Mode.BOOLEAN, "ArrayList", true, "Shows the toggled modules");
		public static Setting mode = new Setting(arrayList, "Mode", "Rainbow", new String[]{"Rainbow", "Use rainbow effect"}, new String[]{"Static", "Use static colors"}, new String[]{"Single", "Uses a single specified color"});
			public static Setting modeSingle = new Setting(mode, "Single", Mode.COLOR, "Color", -1);
			public static Setting modeStaticHash = new Setting(mode, "Static", Mode.TEXT, "Hash", "Sierra", "Chooses what rainbow values each module takes");
		public static Setting sort = new Setting(arrayList, "Sort", "Width", new String[]{"Width", "Sort based on width"}, new String[]{"Alphabet", "Sort alphabetically"}, new String[]{"Config", "Sorts them based on how many config options they have"});
		public static Setting scaleSetting = new Setting(arrayList, Mode.DOUBLE, "Scale", new SettingValue(1, 0.3, 3, 0.1), "Scale for entire thing");
		public static Setting gapSetting = new Setting(arrayList, Mode.INTEGER, "Gap", new SettingValue(8, 3, 25, 1), "Gap between the modules");
		public static Setting background = new Setting(arrayList, Mode.COLOR, "Background", 0, "Background color for all the modules", "Tip: Set alpha to 0 to disable!");

	public ArrayListComponent() {
		super(HudCorner.BOTTOM_RIGHT, arrayList);
		this.defaultY = 2;
		this.defaultX = 0;
	}

	@Override
	public void onRender(DrawContext context, float partialTicks) {
		MatrixStack stack = context.getMatrices();

		float scale = scaleSetting.asFloat();
		float gap = gapSetting.asInt();

		stack.push();
		stack.scale(scale, scale, scale);

		//Sort the list
		if (lastArraylistSize != arraylist.size() || !sort.string().equals(lastSortMode)) {
			if (sort.string().equals("Width")) {
				arraylist.sort(Comparator.comparingInt(m -> Gui.fontRenderer.getWidth(stack, m.name)));
			} else if (sort.string().equals("Alphabet")) {
				arraylist.sort(Comparator.comparing(m -> m.name));
			} else {
				arraylist.sort(Comparator.comparingInt(m -> m.getGuiNode().parentedNodes.size()));
			}

			Collections.reverse(arraylist);
		}

		lastSortMode = sort.string();

		int amount = 0;
		ArrayList<Mod> temp = new ArrayList<>(arraylist);
		for (Mod module : temp) {
			if (module.isOn() && !module.isHidden()) {
				String text = module.name;
				if (module.renderNumber != -1) {
					text += " " + g + "[" + w + module.renderNumber + g + "]";
				}

				int color;
				if (mode.string().equals("Rainbow")) {
					color = getRainbow();
				} else if (mode.string().equals("Single")) {
					color = modeSingle.asInt();
				} else {
					String input = module.name + modeStaticHash.string();
					Random random = new Random(input.hashCode());
					RainbowUtil rainbow = new RainbowUtil();
					color = rainbow.getRainbowColorAt(random.nextInt(356));
				}

				if (corner == HudCorner.BOTTOM_RIGHT || corner == HudCorner.BOTTOM_LEFT) {
					drawString(context, text, 0, -(amount * gap), color, scale);
				} else {
					drawString(context, text, 0, amount * gap, color, scale);
				}

				amount++;
				factorRainbow();
			}
		}
		
		lastArraylistSize = arraylist.size();
		stack.pop();
	}
}
