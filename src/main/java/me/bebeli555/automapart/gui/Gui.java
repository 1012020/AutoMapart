package me.bebeli555.automapart.gui;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.ClientTickEvent;
import me.bebeli555.automapart.events.game.KeyInputEvent;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.windows.games.minesweeper.MineSweeperGameWindow;
import me.bebeli555.automapart.gui.windows.windows.games.snake.SnakeGameWindow;
import me.bebeli555.automapart.gui.windows.windows.games.tetris.TetrisGameWindow;
import me.bebeli555.automapart.gui.windows.windows.other.*;
import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.gui.windows.windows.pickers.pickers.SettingModePicker;
import me.bebeli555.automapart.gui.windows.windows.tools.ReflectorTool;
import me.bebeli555.automapart.gui.windows.windows.tools.TextureEditorTool;
import me.bebeli555.automapart.hud.HudEditor;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.settings.Keybind;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.Settings;
import me.bebeli555.automapart.utils.ClientUtils;
import me.bebeli555.automapart.utils.RainbowUtil;
import me.bebeli555.automapart.utils.font.ColorHolder;
import me.bebeli555.automapart.utils.font.SierraFontRenderer;
import me.bebeli555.automapart.utils.input.Keyboard;
import me.bebeli555.automapart.utils.input.Mouse;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This is built on top of the old CookieClient GUI base, which works but is pretty bad
 * A complete rewrite would be in order
 */
public class Gui extends Screen {
	public static SierraFontRenderer fontRenderer = new SierraFontRenderer(){
		public double getFontSize() {return ClientSettings.font2dSize.asDouble();}
		public int getYAdd() {return ClientSettings.font2dYAdd.asInt();}
		public int getGap() {return ClientSettings.font2dGap.asInt();}
		public String getFontName() {return ClientSettings.font2dName.string();}
		public String getFontType() {return ClientSettings.font2dType.string();}
		public boolean isShadow() {return ClientSettings.font2dShadow.bool();}
		public int getShadowColor() {return ClientSettings.font2dShadowColor.asInt();}
		public int getShadowOffset() {return ClientSettings.font2dShadowOffset.asInt();}
	};

	public static boolean openGui;
	public static MinecraftClient mc = MinecraftClient.getInstance();
	public static List<GuiClick> visibleNodes = new ArrayList<>();
	public static boolean isOpen;
	public static GuiClick selected, description, sliderSelected;
	public static String selectedOldValue;
	public static Group dragging;
	public static int lastMouseX, lastMouseY;
	public static int realLastMouseX, realLastMouseY;
	public static boolean pasting;
	public static char pasteChar;
	public static Gui registerGui = new Gui();
	public static Gui gui = new Gui();
	public static List<Double[]> groupCoords = new ArrayList<>();
	public static double lastGuiScale;

	//Rainbow
	public RainbowUtil rainbow = new RainbowUtil();
	public int rainbowI = 0;

	public Gui() {
		super(Text.literal(Mod.NAME));
	}

	@Override
	public void init() {
		super.init();
		gui = this;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void render(DrawContext context, int realMouseX, int realMouseY, float delta) {
		MatrixStack stack = context.getMatrices();

		//Scale the gui to match the resolution and the gui scale.
		float guiScale = getGuiScale();
		final int mouseX = (int)(realMouseX / guiScale);
		final int mouseY = (int)(realMouseY / guiScale);

		stack.push();
		stack.scale(guiScale, guiScale, guiScale);
		lastGuiScale = guiScale;

		//Draw background
		if (!HudEditor.INSTANCE.isOn()) {
			drawRect(stack, 0, 0, 2000, 2000, ClientSettings.background.asInt());
		}
		
		visibleNodes.clear();
		description = null;

		//Drag group
		if (dragging != null) {
			if (Mouse.isButtonDown(0)) {
				if (mouseY > 10) {
					dragging.x += mouseX - lastMouseX;
					dragging.y += mouseY - lastMouseY;
					updateGuiGroups();
				}
			} else {
				dragging = null;
			}
		}

		//Slider for int and double
		if (sliderSelected != null) {
			if (Mouse.isButtonDown(0)) {
				if (realMouseX - realLastMouseX != 0) {
					int check = (int)(Math.abs(sliderSelected.guiNode.setting.minValue - sliderSelected.guiNode.setting.maxValue) / sliderSelected.guiNode.setting.step);
					int amount = Math.abs(realMouseX - sliderSelected.guiNode.lastSliderX);
					if (amount * check > 80) {
						double d = sliderSelected.guiNode.setting.asDouble();
						d += (realMouseX - realLastMouseX) * sliderSelected.guiNode.setting.step;
						d = new BigDecimal(d).setScale(8, RoundingMode.HALF_UP).doubleValue();

						if (d < sliderSelected.guiNode.setting.minValue && !ClientSettings.bypassLimits.bool()) {
							d = sliderSelected.guiNode.setting.minValue;
						} else if (d > sliderSelected.guiNode.setting.maxValue && !ClientSettings.bypassLimits.bool()) {
							d = sliderSelected.guiNode.setting.maxValue;
						}

						String value = "" + d;
						if (value.endsWith(".0")) {
							value = "" + (int)d;
						}

						sliderSelected.guiNode.stringValue = "" + value;
						sliderSelected.guiNode.setSetting();
						sliderSelected.guiNode.lastSliderX = realMouseX;
					}
				}
			} else {
				sliderSelected = null;
			}
		}

		//Window toggles
		Object[][] windows = {
				{ClientSettings.contactsWindow, ContactsWindow.INSTANCE},
				{ClientSettings.changelogWindow, ChangeLogWindow.INSTANCE},
				{ClientSettings.tipsWindow, TipsWindow.INSTANCE},
				{ClientSettings.snakeGame, SnakeGameWindow.INSTANCE},
				{ClientSettings.tetrisGame, TetrisGameWindow.INSTANCE},
				{ClientSettings.mineSweeperGame, MineSweeperGameWindow.INSTANCE},
				{ClientSettings.reflectorTool, ReflectorTool.INSTANCE},
				{ClientSettings.textureEditorTool, TextureEditorTool.INSTANCE}
		};

		for (Object[] object : windows) {
			Setting setting = (Setting)object[0];
			TitledWindow window = (TitledWindow)object[1];
			if (setting.bool() && !window.isToggled()) {
				window.enable(mouseX, mouseY);
			} else if (!setting.bool() && window.isToggled()) {
				window.disable();
			}
		}

		//Rainbow
		rainbowI = 0;
		rainbow.setSpeed(ClientSettings.rainbowBorderSpeed.asInt());
		rainbow.onUpdate();

		//Check hovering over
		boolean hoveringOverWindow = false;
		for (TitledWindow window : TitledWindow.list) {
			if (window.isToggled() && window.isHoveringOver(mouseX, mouseY)) {
				if (!HudEditor.INSTANCE.isOn() || window instanceof ColorPickerWindow || window instanceof TextSetterWindow || window instanceof PickerWindow) {
					hoveringOverWindow = true;
					break;
				}
			}
		}

		//Draw all visible nodes
		for (Group.GroupWindow groupWindow : Group.valuesSortedByXGroupWindows()) {
			rainbowI += ClientSettings.rainbowBorderGroupFactor.asInt();
			if (rainbowI > 355) {
				rainbowI = 0;
			}

			if (groupWindow.window != null) {
				groupWindow.window.rainbowI = rainbowI;
				continue;
			}

			//Hudeditor is open
			Group group = groupWindow.group;
			if (HudEditor.INSTANCE.isOn() && group != Group.CLIENT) {
				continue;
			}

			//Don't allow group to go off bounds
			if (mc.getWindow().isFullscreen() && mc.getWindow().getWidth() == mc.getWindow().getMonitor().getCurrentVideoMode().getWidth() && ClientSettings.guiKeepInBounds.bool()) {
				double scale = SierraFontRenderer.getScale(stack);
				int safe = 20;

				if (group.x -safe < -ClientSettings.width.asDouble()) {
					group.x = -ClientSettings.width.asDouble() + safe;
				} else if (group.x + safe > mc.getWindow().getScaledWidth() / scale) {
					group.x = mc.getWindow().getScaledWidth() / scale - safe;
				}
			}

			//All visible nodes
			if (group.extended) {
				List<GuiNode> visibleNodes = new ArrayList<>();
				for (GuiNode node : GuiNode.all) {
					if (HudEditor.INSTANCE.isOn() && (!node.id.toLowerCase().startsWith("hud") && !node.id.toLowerCase().startsWith("font") && !node.id.toLowerCase().startsWith("client"))) {
						continue;
					}

					if (node.isVisible && node.group == group && node.shouldRender()) {
						if (node.setting != null && !node.setting.isVisible.getAsBoolean()) {
							continue;
						}

						visibleNodes.add(node);
					}
				}

				int count = 0;
				for (GuiNode node : visibleNodes) {
					drawGuiNode(stack, mouseX, mouseY, node, count, visibleNodes.size(), hoveringOverWindow);
					count++;
				}
			}

			//Draw the Group thing
			double x = group.x;
			double x2 = group.x + ClientSettings.width.asDouble();
			double y = group.y - ClientSettings.height.asDouble();
			double y2 = group.y;
			GuiNode guiNode = new GuiNode(true);
			guiNode.description = group.description;
			GuiClick guiClick = new GuiClick(x, y, x2, y2, guiNode);

			group.renderX = x;
			group.renderY = y;
			group.renderX2 = x2;
			group.renderY2 = y2;

			drawRect(stack, x, y, x2, y2, ClientSettings.groupBackground.asInt());
			drawBorder(stack, true, true, true, !group.extended, guiClick, guiClick, false, rainbow.getRainbowColorAt(rainbowI));
			float scale = (float) ClientSettings.groupScale.asDouble();
			stack.push();
			stack.scale(scale, scale, scale);
			drawString(stack, group.name, (float)((((x2 / scale) - (x / scale)) / 2) + (x / scale) - (((float)fontRenderer.getWidth(stack, group.name)) / 2)), (float)y / scale + 4 / scale, ClientSettings.groupTextColor.asInt());
			stack.pop();

			if (x < mouseX && x2 > mouseX && y < mouseY && y2 > mouseY) {
				description = guiClick;

				//Drag the group if holding mouse
				if (Mouse.isButtonDown(0)) {
					if (dragging == null) {
						boolean windowDrag = false;
						for (TitledWindow window : TitledWindow.list) {
							if (window.dragging && window.isToggled()) {
								windowDrag = true;
								break;
							}
						}

						if (!windowDrag) {
							dragging = group;
						}
					}
				}
			}
		}

		//Draw titled windows if hudeditor isnt open
		java.util.List<TitledWindow> windowList = new ArrayList<>(TitledWindow.list);
		Collections.reverse(windowList);
		for (TitledWindow window : windowList) {
			if (HudEditor.INSTANCE.isOn() && !(window instanceof ColorPickerWindow) && !(window instanceof TextSetterWindow) && !(window instanceof PickerWindow)) {
				continue;
			}

			window.drawWindow(context, mouseX, mouseY, lastMouseX, lastMouseY);
		}

		//Draw descriptions so they will overlay everything else
		for (GuiClick g : new GuiClick[]{selected, description}) {
			if (g != null) {
				String[] description = g.guiNode.description;

				if (ClientSettings.guiShowIds.bool() && g.guiNode.id != null && description != null) {
					description = Mod.addToArray(description, "ID: " + g.guiNode.id);
				}

				if (g.guiNode.modes.size() != 0) {
					Object[] array;
					try {
						array = g.guiNode.modeDescriptions.get(g.guiNode.modes.indexOf(g.guiNode.stringValue)).toArray();
					} catch (Exception e) {
						//This is probably caused because the saved setting had a mode value that is no longer existing so it will just set it to the default
						//Probably because a new version update that modified the mode names
						g.guiNode.stringValue = g.guiNode.modes.get(0);
						break;
					}

					description = Arrays.copyOf(array, array.length, String[].class);
				}
				
		 		for (GuiNode node : g.guiNode.parentedNodes) {
					if (!node.modeName.isEmpty() && !node.modeName.equals(g.guiNode.stringValue)) {
						continue;
					}

					if (node.setting != null && !node.setting.isVisible.getAsBoolean()) {
						continue;
					}

					String text = g.guiNode.isExtended ? "Right click to shrink" : "Right click to extend";
					if (description == null || description.length == 0) {
						description = new String[]{new ColorHolder(ClientSettings.descriptionClickText.asInt()) + text};
					} else {
						description = Mod.addToArray(description, new ColorHolder(ClientSettings.descriptionClickText.asInt()) + text);
					}
					break;
		 		}
				
				if (selected != null && selected.equals(g)) {
					if (g.guiNode.onlyNumbers) {
						description = new String[]{new ColorHolder(ClientSettings.descriptionSetText.asInt()) + "Type numbers in your keyboard to set this"};
					} else if (g.guiNode.isKeybind) {
						description = new String[]{new ColorHolder(ClientSettings.descriptionSetText.asInt()) + "Click a key to set the keybind"};
					} else {
						description = new String[]{new ColorHolder(ClientSettings.descriptionSetText.asInt()) + "Type with your keyboard to set this"};
					}
				}

				if (description != null && !hoveringOverWindow) {
					int longestWidth = 0;
					boolean left = false;
					for (String s : description) {
						int width = fontRenderer.getWidth(stack, s);
						width += Math.abs(g.x - g.x2) + 6;
						
						if (width > longestWidth) {
							longestWidth = width;
							
							if (g.x + width > (double)mc.getWindow().getWidth() / 2) {
								left = true;
							}
						}
					}
					
					for (int i = 0; i < description.length; i++) {
						int y = (int)(g.y + 6 + (i * 11));
						int width = fontRenderer.getWidth(stack, description[i]);
						
						if (left) {
							drawRect(stack, g.x2 - longestWidth, y - 2, g.x - 2, y + 9, ClientSettings.descriptionBackground.asInt());
							drawString(stack, description[i], (float)((g.x2) - longestWidth + 2), (float)((g.y + 7) + (i * 10)), ClientSettings.descriptionText.asInt());
						} else {
							drawRect(stack, g.x2 + 8, y - 2, g.x2 + width + 12, y + 9, ClientSettings.descriptionBackground.asInt());
							drawString(stack, description[i], (float)((g.x2) + 10), (float)((g.y + 7) + (i * 10)), ClientSettings.descriptionText.asInt());
						}
					}
				}
				
				break;
			}
		}

		lastMouseX = mouseX;
		lastMouseY = mouseY;
		realLastMouseX = realMouseX;
		realLastMouseY = realMouseY;
		stack.pop();
	}

	//This calculates the coordinates for the node and draws everything for the node.
	public GuiClick lastG;
	public void drawGuiNode(MatrixStack stack, int mouseX, int mouseY, GuiNode node, int aboveNodes, int nodes, boolean hoveringOverWindow) {
		int extendMoveMultiplier = node.getAllParents().size() * ClientSettings.extendMove.asInt();
		double yLevel = (aboveNodes * (ClientSettings.height.asDouble()));
		GuiClick g = new GuiClick(node.group.x + extendMoveMultiplier, 
				node.group.y + yLevel,
				node.group.x + ClientSettings.width.asDouble() + extendMoveMultiplier,
				node.group.y + ClientSettings.height.asDouble() + yLevel - ClientSettings.gapBorderSize.asDouble(), node);
		
		//Draw box
		int boxColor = ClientSettings.backgroundColor.asInt();
		if (node.toggled && !node.isLabel && !node.isColor && !node.isPicker) {
			boxColor = ClientSettings.enabledBackground.asInt();
		} else if (node.getAllParents().size() > 0) {
			boxColor = ClientSettings.extendBackground.asInt();
		}

		boolean hoveringOver = g.x < mouseX && g.x2 > mouseX && g.y < mouseY && g.y2 > mouseY;
		if (hoveringOver && !hoveringOverWindow) {
			boxColor = alphaAdd(boxColor, new Color(ClientSettings.hoverAlpha.asInt(), true).getAlpha());
		}

		boolean searchCheck = node.id.toLowerCase().contains(ClientSettings.guiSearch.string().toLowerCase());
		if (node.id.equals("Find")) {
			searchCheck = true;
		}

		if (!searchCheck) {
			boxColor = ClientSettings.backgroundColor.asInt();
		}

		if (HudEditor.INSTANCE.isOn()) {
			drawRect(stack, g.x, g.y, g.x2, g.y2, HudEditor.nodeBackground.asInt());
		}

		drawRect(stack, g.x, g.y, g.x2, g.y2, boxColor);

		//Draw slider
		if (g.guiNode.setting != null && g.guiNode.setting.hasSetMinAndMaxValues() && searchCheck) {
			double value = Math.abs(g.guiNode.setting.minValue - g.guiNode.setting.asDouble()) / Math.abs(g.guiNode.setting.minValue - g.guiNode.setting.maxValue);
			int sliderColor = ClientSettings.sliderBackground.asInt();
			if (hoveringOver) {
				sliderColor = alphaAdd(sliderColor, ClientSettings.hoverAlpha.asInt());
			}

			drawRect(stack, g.x, g.y, (int)(g.x + (g.x2 - g.x) * value), g.y2, sliderColor);
		}

		//Draw text if it's too big for the width then lower the scale on it
		String text = g.guiNode.name;
		if (g.guiNode.isTypeable && searchCheck && !g.guiNode.isTextArray) {
			ColorHolder color = new ColorHolder(ClientSettings.typeableTextName.asInt());
			if (g.guiNode.isKeybind) {
				String value = g.guiNode.renderValue.isEmpty() ? new ColorHolder(ClientSettings.noneText.asInt()) + "NONE" : new ColorHolder(ClientSettings.typeableTextValue.asInt()) + g.guiNode.renderValue;
				text = new ColorHolder(ClientSettings.keybindText.asInt()) + g.guiNode.name + ": " + value;
			} else if (g.guiNode.stringValue.isEmpty()) {
				g.guiNode.stringValue = "";
				text = color + g.guiNode.name + ": " + new ColorHolder(ClientSettings.noneText.asInt()) + "NONE";
			} else {
				text = color + g.guiNode.name + ": " + new ColorHolder(ClientSettings.typeableTextValue.asInt()) + g.guiNode.stringValue;
			}
		} else if (g.guiNode.modes.size() != 0 && searchCheck && !g.guiNode.isTextArray) {
			text = new ColorHolder(ClientSettings.modeTextName.asInt()) + g.guiNode.name + ": " + new ColorHolder(ClientSettings.modeTextValue.asInt()) + g.guiNode.stringValue;
		}

		//Draw color rectangle
		if (node.isColor && searchCheck) {
			int gScale = (int)(Math.abs(g.y - g.y2) - 5);
			drawRect(stack, g.x2 - gScale, g.y2 - gScale, g.x2 - 5, g.y2 - 5, node.setting.asInt());
		}

		float scale = 1F;
		boolean scaled = false;
		if (fontRenderer.getWidth(stack, text) > (g.x2 - 3) - g.x && searchCheck) {
			stack.push();
			scale = (float)(((g.x2 - 3) - g.x) / fontRenderer.getWidth(stack, text));
			stack.scale(scale, scale, scale);
			scaled = true;
		}

		//This is some serious math
		if (searchCheck) {
			drawString(stack, text, (float)((((g.x2 / scale) - (g.x / scale)) / 2) + (g.x / scale) - ((fontRenderer.getWidth(stack, text)) / 2)), (float)((g.y + (g.y2 - g.y) / 3) / scale), g.guiNode.getTextColor());
		}

		if (scaled) {
			stack.pop();
		}

		//Draw border
		drawBorder(stack, true, true, aboveNodes != 0, aboveNodes + 1 == nodes, g, lastG, true, rainbow.getRainbowColorAt(rainbowI));
		
		//also calculate the thing to draw above it so it will match the other border if its more x than it is
		if (!visibleNodes.isEmpty()) {
			GuiClick last = visibleNodes.get(visibleNodes.size() - 1);

			if (last.guiNode.group == node.group) {
				int color = ClientSettings.borderColor.asInt();
				if (ClientSettings.rainbowBorder.bool()) {
					Color rainbowColor = new Color(rainbow.getRainbowColorAt(rainbowI));
					Color color2 = new Color(rainbowColor.getRed(), rainbowColor.getGreen(), rainbowColor.getBlue(), ClientSettings.rainbowBorderAlpha.asInt());
					color = color2.getRGB();
				}

				double moved = Math.abs(last.x - g.x);

				//Last is more on left
	 			if (last.x < g.x) {
					drawLine(stack, last.x2 + ClientSettings.borderSize.asDouble(), g.y - ClientSettings.borderSize.asDouble(), last.x2 + ClientSettings.borderSize.asDouble() + moved, g.y, color);
					drawLine(stack, last.x, g.y - ClientSettings.borderSize.asDouble(), last.x + moved, g.y, color);
				}
	 			
	 			//Last is more on right
	 			else if (last.x > g.x) {
					drawLine(stack, last.x2 - moved, g.y - ClientSettings.borderSize.asDouble(), last.x2, g.y, color);
					drawLine(stack, last.x - moved - ClientSettings.borderSize.asDouble(), g.y - ClientSettings.borderSize.asDouble(), last.x - ClientSettings.borderSize.asDouble(), g.y, color);
	 			}
			}
		}
		
		//Set description
		if (g.x < mouseX && g.x2 > mouseX && g.y < mouseY && g.y2 > mouseY && searchCheck) {
			description = g;
		}
		
		//Add GuiClick to visibleNodes list
		visibleNodes.add(g);
		lastG = g;
	}

	public static void drawBorder(MatrixStack stack, boolean right, boolean left, boolean up, boolean down, GuiClick n, GuiClick last, boolean gapUp, int rainbow) {
		int borderColor = ClientSettings.borderColor.asInt();
		if (ClientSettings.rainbowBorder.bool()) {
			Color rainbowColor = new Color(rainbow);
			Color color = new Color(rainbowColor.getRed(), rainbowColor.getGreen(), rainbowColor.getBlue(), ClientSettings.rainbowBorderAlpha.asInt());
			borderColor = color.getRGB();
		}

		if (last == null) last = n;
		if (up) drawLine(stack, (Math.max(n.x, last.x)) - (gapUp ? 0 : ClientSettings.borderSize.asDouble()), n.y - (gapUp ? ClientSettings.gapBorderSize.asDouble() : ClientSettings.borderSize.asDouble()), Math.min(n.x2, last.x2) + (gapUp ? 0 : ClientSettings.borderSize.asDouble()), n.y, gapUp ? ClientSettings.gapBorder.asInt() : borderColor);
		if (down) drawLine(stack, n.x - ClientSettings.borderSize.asDouble(), n.y2, n.x2 + ClientSettings.borderSize.asDouble(), n.y2 + ClientSettings.borderSize.asDouble(), borderColor);
		if (left) drawLine(stack, n.x - ClientSettings.borderSize.asDouble(), n.y, n.x, n.y2 + (gapUp ? ClientSettings.gapBorderSize.asDouble() : 0), borderColor);
		if (right) drawLine(stack, n.x2 + ClientSettings.borderSize.asDouble(), n.y, n.x2, n.y2 + (gapUp ? ClientSettings.gapBorderSize.asDouble() : 0), borderColor);
	}

	public int alphaAdd(int hex, int alpha) {
		Color color = new Color(hex, true);
		color = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(color.getAlpha() + alpha, 255));
		return color.getRGB();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		//Just use the lastMouse positions saved by the rendering loop because these are different
		int x = lastMouseX;
		int y = lastMouseY;

		//Group
		for (Group group : Group.values()) {
			if (x > group.renderX && x < group.renderX2 && y > group.renderY && y < group.renderY2) {
				if (button == 1) {
					group.extended = !group.extended;
				} else if (button == 2) {
					group.x = group.defaultX;
					group.y = group.defaultY;
					updateGuiGroups();
				}

				break;
			}
		}

		for (TitledWindow window : new ArrayList<>(TitledWindow.list)) {
			if (window.isToggled()) {
				if (HudEditor.INSTANCE.isOn() && !(window instanceof ColorPickerWindow) && !(window instanceof TextSetterWindow) && !(window instanceof PickerWindow)) {
					continue;
				}

				if (!window.isHoveringOver(x, y)) {
					window.onOutsideClick();
					continue;
				}

				window.onClickParent(x, y, button);
				return true;
			}
		}

		if (selected != null) {
			if (selected.guiNode.setting != null && selected.guiNode.setting.hasSetMinAndMaxValues()) {
				try {
					double value = Double.parseDouble(selected.guiNode.stringValue);
					if (value > selected.guiNode.setting.maxValue && !ClientSettings.bypassLimits.bool()) {
						selected.guiNode.stringValue = selectedOldValue;
						ClientUtils.sendMessage("Max value for " + selected.guiNode.name + " is " + selected.guiNode.setting.maxValue, Formatting.RED, "GUI");
					} else if (value < selected.guiNode.setting.minValue && !ClientSettings.bypassLimits.bool()) {
						selected.guiNode.stringValue = selectedOldValue;
						ClientUtils.sendMessage("Min value for " + selected.guiNode.name + " is " + selected.guiNode.setting.minValue, Formatting.RED, "GUI");
					}
				} catch (Exception ignored) {}
			}

			if (selected.guiNode.stringValue.isEmpty() && selected.guiNode.setting != null && selected.guiNode.setting.hasSetMinAndMaxValues()) {
				selected.guiNode.setDefaultValue();
			}

			selected.guiNode.setSetting();
		}
		selected = null;

		for (GuiClick guiClick : visibleNodes) {
			if (guiClick.x < x && guiClick.x2 > x && guiClick.y < y && guiClick.y2 > y) {
				if (button == 1) {
					if (guiClick.guiNode.isTypeable && !guiClick.guiNode.onlyNumbers && !TextSetterWindow.INSTANCE.isToggled() && guiClick.guiNode.modes.size() == 0 && !guiClick.guiNode.isKeybind && !guiClick.guiNode.isTextArray) {
						TextSetterWindow.currentNode = guiClick.guiNode;
						TextSetterWindow.INSTANCE.enable(x, y);
					}

					if (guiClick.guiNode.isTextArray && !TextArrayWindow.INSTANCE.isToggled()) {
						TextArrayWindow.currentNode = guiClick.guiNode;
						TextArrayWindow.INSTANCE.enable(x, y);
					}

					if (guiClick.guiNode.setting != null && guiClick.guiNode.setting.hasSetMinAndMaxValues()) {
						selected = guiClick;
						selectedOldValue = selected.guiNode.stringValue;
						guiClick.guiNode.click(x, y, button);
					}

					if (!guiClick.guiNode.parentedNodes.isEmpty()) {
						int index = 0;
						if (!guiClick.guiNode.modes.isEmpty()) {
							for (int i = 0; i < guiClick.guiNode.parentedNodes.size(); i++) {
								GuiNode node = guiClick.guiNode.parentedNodes.get(i);
								if (!node.modeName.isEmpty() && node.modeName.equals(guiClick.guiNode.stringValue)) {
									index = i;
									break;
								}
							}
						}
						
						guiClick.guiNode.extend(!guiClick.guiNode.parentedNodes.get(index).isVisible);
					}
				} else if (button == 2) {
					if (guiClick.guiNode.isExtended) {
						guiClick.guiNode.extend(false);
					}

					boolean prevToggled = guiClick.guiNode.toggled;
					guiClick.guiNode.setDefaultValue();

					if (guiClick.guiNode.setting != null && guiClick.guiNode.setting.mode == Mode.PICKER) {
						guiClick.guiNode.setting.getPickerWindow().resetToDefaults(guiClick.guiNode.setting);
					} else if (guiClick.guiNode.setting != null && guiClick.guiNode.setting.mode == Mode.BOOLEAN) {
						for (Mod mod : Mod.modules) {
							if (mod.name.equals(guiClick.guiNode.id)) {
								if (prevToggled && !guiClick.guiNode.toggled && mod.isOn()) {
									mod.disable();
								} else if (!prevToggled && guiClick.guiNode.toggled && !mod.isOn()) {
									mod.enable();
								}
							}
						}
					}
				} else {
					if (guiClick.guiNode.setting != null && guiClick.guiNode.setting.hasSetMinAndMaxValues()) {
						sliderSelected = guiClick;
					} else if (guiClick.guiNode.isTypeable && !guiClick.guiNode.isTextArray) {
						selected = guiClick;
						selectedOldValue = selected.guiNode.stringValue;
					}
					
					guiClick.guiNode.click(x, y, button);
				}
			}
		}

		return true;
	}

	@Override
	public boolean charTyped(char key, int keyCode) {
		if (key == '\u0000') {
			return true;
		}

		for (TitledWindow window : TitledWindow.list) {
			if (window.isToggled()) {
				window.onWindowChar(key);
			}
		}

		if (selected != null && !selected.guiNode.isKeybind) {
			char[] acceptedKeys;
			if (selected.guiNode.onlyNumbers) {
				if (selected.guiNode.acceptDoubleValues) {
					acceptedKeys = new char[]{'0','1','2','3','4','5','6','7','8','9','-','.'};
				} else {
					acceptedKeys = new char[]{'0','1','2','3','4','5','6','7','8','9','-'};
				}
			} else {
				selected.guiNode.stringValue += key;
				selected.guiNode.notifyKeyListeners();

				return true;
			}

			//Check if key is in the acceptedKeys list and then put it to the stringValue
			for (char accept : acceptedKeys) {
				if (accept == key) {
					if (key == '-') {
						selected.guiNode.stringValue = "";
					}

					selected.guiNode.stringValue += key;
					selected.guiNode.notifyKeyListeners();
					return true;
				}
			}
		}

		return true;
	}

	@Subscribe
	public void keyInputEvent(KeyInputEvent e) {
		for (TitledWindow window : TitledWindow.list) {
			if (window.isToggled()) {
				window.onWindowKey(e);
			}
		}

		if (description != null && description.guiNode.modes.size() > 0 && e.getKey() == GLFW.GLFW_KEY_SPACE) {
			SettingModePicker.INSTANCE.setMode(description.guiNode.setting);
			SettingModePicker.INSTANCE.enable(description.guiNode.setting, lastMouseX, lastMouseY);
		}

		if (selected != null && Keyboard.isKeyDown(Keyboard.getEventKey())) {
			//Paste
			if (!pasting && Keyboard.isKeyDown(GLFW.GLFW_KEY_V) && Keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
				pasting = true;

				try {
					String clipboard = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);

					for (char c : clipboard.toCharArray()) {
						pasteChar = c;
					}
				} catch (Exception ignored) {

				}

				pasting = false;
				return;
			}

			//Stuff for keybinds
			if (selected.guiNode.isKeybind) {
				if (Keyboard.getEventKey() != GLFW.GLFW_KEY_BACKSPACE) {
					selected.guiNode.setValue("" + Keyboard.getEventKey());
				} else {
					selected.guiNode.setValue("");
				}

				Keybind.setKeybinds();
				selected.guiNode.notifyKeyListeners();
				return;
			}

			//Backspace one key and if already empty then set to default value
			if (Keyboard.isKeyDown(GLFW.GLFW_KEY_BACKSPACE) || Keyboard.isKeyDown(GLFW.GLFW_KEY_DELETE)) {
				if (!selected.guiNode.stringValue.isEmpty()) {
					selected.guiNode.setValue(selected.guiNode.stringValue.substring(0, selected.guiNode.stringValue.length() - 1));
				} else {
					selected.guiNode.setDefaultValue();
				}

				selected.guiNode.notifyKeyListeners();
				return;
			}
		}

		if (selected != null && selected.guiNode.instaType) {
			selected.guiNode.setSetting();
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		boolean hoveringWindow = false;
		for (TitledWindow window : TitledWindow.list) {
			if (window.isToggled()) {
				if (window.isHoveringOver(lastMouseX, lastMouseY)) {
					hoveringWindow = true;
					window.onScroll(true, (int)(verticalAmount * window.scrollAmount));
					break;
				}
			}
		}

		if (!hoveringWindow) {
			for (Group group : Group.values()) {
				group.y += verticalAmount * ClientSettings.scrollAmount.asInt();
			}
		}

		return true;
	}
	
	@Subscribe
	public void tickEvent(ClientTickEvent e) {
		//Have to open the GUI this way because if you try to open it in the chat event it wont work and if you try to put it to a new thread the mouse will be invisible.
		if (openGui) {
			//Set all key and mouse states to off
			Mouse.down.clear();
			Keyboard.down.clear();

			//Reset search
			GuiNode node = Settings.getGuiNodeFromId(ClientSettings.guiSearch.id);
			if (node != null) {
				node.stringValue = "";
				node.setSetting();
			}

			mc.setScreen(new Gui());
			openGui = false;
			isOpen = true;
			updateGuiGroups();

			//Turn off games
			ClientSettings.snakeGame.setValue(false);
			ClientSettings.tetrisGame.setValue(false);
			ClientSettings.mineSweeperGame.setValue(false);

			TetrisGameWindow.INSTANCE.gameOver = true;
			return;
		}
		
		//Save settings when GUI is closed
		if (isOpen && mc.currentScreen == null) {
			for (int i = 0; i < Group.values().length; i++) {
				Group.values()[i].x = groupCoords.get(i)[0];
				Group.values()[i].y = groupCoords.get(i)[1];
			}

			for (TitledWindow window : TitledWindow.list) {
				window.onGuiClose();
				if (window.closeOnGuiClose) {
					window.disable();
				}
			}

			Settings.saveSettings();

			isOpen = false;
			selected = null;
			pasting = false;
			dragging = null;
			description = null;

			Mod.EVENT_BUS.unregister(registerGui);
		}
	}

	public static float getGuiScale() {
		float scale = (float)1 / SierraFontRenderer.getMcScale();
		scale *= ClientSettings.scale.asFloat();

		return scale;
	}

	//Update gui groups. So these are set when gui closes not the scrolled ones or the gui could go out of screen and confusion strikes
	public static void updateGuiGroups() {
		groupCoords.clear();
		
		for (Group group : Group.values()) {
			groupCoords.add(new Double[]{group.x, group.y});
		}
	}

	public static void drawRect(MatrixStack stack, double x, double y, double x2, double y2, int color) {
		if (x < x2) {
			double temp = x;
			x = x2;
			x2 = temp;
		}

		if (y < y2) {
			double temp = y;
			y = y2;
			y2 = temp;
		}

		Matrix4f matrix4f = stack.peek().getPositionMatrix();
		float f = (float) ColorHelper.Argb.getAlpha(color) / 255.0f;
		float g = (float)ColorHelper.Argb.getRed(color) / 255.0f;
		float h = (float)ColorHelper.Argb.getGreen(color) / 255.0f;
		float j = (float)ColorHelper.Argb.getBlue(color) / 255.0f;

		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix4f, (float)x, (float)y, 0).color(g, h, j, f).next();
		bufferBuilder.vertex(matrix4f, (float)x, (float)y2, 0).color(g, h, j, f).next();
		bufferBuilder.vertex(matrix4f, (float)x2, (float)y2, 0).color(g, h, j, f).next();
		bufferBuilder.vertex(matrix4f, (float)x2, (float)y, 0).color(g, h, j, f).next();
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.disableBlend();
	}

	public static void drawLine(MatrixStack stack, double x, double y, double x2, double y2, int color) {
		drawLine(stack, x, y, x2, y2, color, false);
	}

	public static void drawLine(MatrixStack stack, double x, double y, double x2, double y2, int color, boolean translateToTop) {
		DrawContext context1 = new DrawContext(mc, mc.getBufferBuilders().getEntityVertexConsumers());

		float scale = (float)1 / SierraFontRenderer.getMcScale();
		float cScale = (float)SierraFontRenderer.getScale(stack);
		x = x * cScale / scale;
		y = y * cScale / scale;
		x2 = x2 * cScale / scale;
		y2 = y2 * cScale / scale;

		if (Math.round((float)x) == Math.round((float)x2)) {
			x2 = Math.round((float)x) + (x2 < x ? -1 : 1);
		}

		if (Math.round((float)y) == Math.round((float)y2)) {
			y2 = Math.round((float)y) + (y2 < y ? -1 : 1);
		}

		context1.getMatrices().push();
		if (translateToTop) context1.getMatrices().translate(0, 0, 1000);

		context1.getMatrices().scale(scale, scale, scale);
		context1.fill(Math.round((float)x), Math.round((float)y), Math.round((float)x2), Math.round((float)y2), color);

		context1.getMatrices().pop();
	}

	public static void drawString(MatrixStack stack, String text, float x, float y, int color) {
		fontRenderer.drawString(stack, text, x, y, color);
	}
	
	public static class GuiClick {
		public double x, y, x2, y2;
		public GuiNode guiNode;
		
		public GuiClick(double x, double y, double x2, double y2, GuiNode guiNode) {
			this.x = x;
			this.y = y;
			this.x2 = x2;
			this.y2 = y2;
			this.guiNode = guiNode;
		}
	}
}
