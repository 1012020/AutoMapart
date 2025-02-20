package me.bebeli555.automapart.mods;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Group;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.hud.HudEditor;
import me.bebeli555.automapart.utils.ClientUtils;
import me.bebeli555.automapart.utils.Renderer2DIn3D;
import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingList;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.utils.font.SierraFontRenderer;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientSettings extends Mod {
	public ClientSettings() {
		super(Group.CLIENT);
	}

	public static Setting hud = new Setting(Mode.BOOLEAN, "HUD", true, "Settings about the HUD");
		public static Setting hudScale = new Setting(hud, Mode.DOUBLE, "Scale", new SettingValue(3.5, 1, 6, 0.05), "How much more to scale it than default", "Higher = bigger");
		public static Setting hudRainbow = new Setting(hud, Mode.LABEL, "Rainbow", true, "Global rainbow settings for the HUD");
			public static Setting hudRainbowSpeed = new Setting(hudRainbow, Mode.INTEGER, "Speed",  new SettingValue(9, 0, 100), "Rainbow speed");
			public static Setting hudRainbowFactor = new Setting(hudRainbow, Mode.INTEGER, "Factor",  new SettingValue(20, 0, 100), "How much to change when rotating between items");
		public static SettingList hudComponents = new SettingList();
		static {
			HudEditor.initComponents();

			for (HudComponent component : HudComponent.components) {
				component.mainSetting.parent = hud;
				component.mainSetting.setId();
				hudComponents.list.add(component.mainSetting);

				for (Setting child : component.mainSetting.getChildren("HUD")) {
					if (!child.getParents().contains(hud)) {
						continue;
					}

					child.setId();
					hudComponents.list.add(child);
				}
			}
		}


	public static Setting guiSettings = new Setting(Mode.LABEL, "GUI", true, "Settings about the GUI design");
		public static Setting guiSettingColors = new Setting(guiSettings, Mode.LABEL, "Colors", true, "GUI color settings");
			public static Setting borderColor = new Setting(guiSettingColors, Mode.COLOR, "Border color", -9109785, "Color of the border in hex and with 0xAA");
			public static Setting backgroundColor = new Setting(guiSettingColors, Mode.COLOR, "Color", 1918304256, "The background color of fields");
			public static Setting textColor = new Setting(guiSettingColors, Mode.COLOR, "Text Color", -1973791, "Text color when module is toggled on");
			public static Setting textColorOff = new Setting(guiSettingColors, Mode.COLOR, "Text Color Off", -1973791, "Text color when module is toggled off");
			public static Setting labelColor = new Setting(guiSettingColors, Mode.COLOR, "Label color", -47546, "The color of the label text which is an toggleable module");
			public static Setting colorColor = new Setting(guiSettingColors, Mode.COLOR, "Color color", -1, "The color of the color mode that opens colorpicker");
			public static Setting groupTextColor = new Setting(guiSettingColors, Mode.COLOR, "Group color", -1, "The text color of the group");
			public static Setting groupBackground = new Setting(guiSettingColors, Mode.COLOR, "Group background", -6356224, "The group background color");
			public static Setting sliderBackground = new Setting(guiSettingColors, Mode.COLOR, "Slider background", -1515585536, "The number slider background color");
			public static Setting enabledBackground = new Setting(guiSettingColors, Mode.COLOR, "ON background", -1515585536, "Background color for enabled modules");
			public static Setting modeTextName = new Setting(guiSettingColors, Mode.COLOR, "ModeTextName", -1994326, "Text color for mode fields name");
			public static Setting modeTextValue = new Setting(guiSettingColors, Mode.COLOR, "ModeTextValue", -1, "Text color for mode fields value");
			public static Setting typeableTextName = new Setting(guiSettingColors, Mode.COLOR, "TypeableTextName", -5461334, "Text color for the name in a typeable field");
			public static Setting typeableTextValue = new Setting(guiSettingColors, Mode.COLOR, "TypeableTextValue", -1, "Text color for the value in a typeable field");
			public static Setting keybindText = new Setting(guiSettingColors, Mode.COLOR, "KeybindText", -38809, "Text color for the Keybind: text in keybind field");
			public static Setting noneText = new Setting(guiSettingColors, Mode.COLOR, "NoneText", -1, "Text color for a NONE value in a field");
			public static Setting descriptionBackground = new Setting(guiSettingColors, Mode.COLOR, "DescBackground", -16777216, "Background fill color for descriptions");
			public static Setting descriptionText = new Setting(guiSettingColors, Mode.COLOR, "DescText", -1, "Color for a descriptions text");
			public static Setting descriptionClickText = new Setting(guiSettingColors, Mode.COLOR, "DescClickText", -16715767, "Color for a click tip in description", "Like right click to extend etc");
			public static Setting descriptionSetText = new Setting(guiSettingColors, Mode.COLOR, "DescSetText", -1069641, "Color for a set tip in description", "Like type numbers to set this etc");
			public static Setting background = new Setting(guiSettingColors, Mode.COLOR, "Background", -1946157056, "GUI background color");
			public static Setting hoverAlpha = new Setting(guiSettingColors, Mode.COLOR, "HoverAlpha", 1308622847, "Only alpha for field hovering over");
			public static Setting extendBackground = new Setting(guiSettingColors, Mode.COLOR, "ExtendBackground", 1513291776, "Background color for extended field");
			public static Setting gapBorder = new Setting(guiSettingColors, Mode.COLOR, "GapBorder", 844759040, "Color for the gap borders");
			public static Setting blockPickerText = new Setting(guiSettingColors, Mode.COLOR, "BlockPickerText", -1, "Color for block picker field text");


		public static Setting titledWindow = new Setting(guiSettings, Mode.LABEL, "TitledWindow", true, "GUI color/value settings for windows like colorpicker");
			public static Setting titledWindowWindow = new Setting(titledWindow, Mode.LABEL, "Window", true, "The actual window settings");
				public static Setting titledWindowBackground = new Setting(titledWindowWindow, Mode.COLOR, "Background", -1270545920);
				public static Setting titledWindowTitleText = new Setting(titledWindowWindow, Mode.COLOR, "TitleText", -1);
				public static Setting titledWindowText = new Setting(titledWindowWindow, Mode.COLOR, "Text", -4605255, "Text color for example changelog");
				public static Setting titledWindowBorder = new Setting(titledWindowWindow, Mode.BOOLEAN, "Border", true, "Border settings");
					public static Setting titledWindowBorderColor = new Setting(titledWindowBorder, Mode.COLOR, "Color", -1);
					public static Setting titledWindowBorderRainbow = new Setting(titledWindowBorder, Mode.BOOLEAN, "Rainbow", true, "Applies GUI border rainbow to windows");
					public static Setting titledWindowBorderSize = new Setting(titledWindowBorder, Mode.DOUBLE, "Size", new SettingValue(0.4, 0.1, 5, 0.1));
			public static Setting titledWindowButton = new Setting(titledWindow, Mode.LABEL, "Button", true, "Window button settings");
				public static Setting titledWindowButtonText = new Setting(titledWindowButton, Mode.COLOR, "ButtonText", -4605255);
				public static Setting titledWindowButtonBackground = new Setting(titledWindowButton, Mode.COLOR, "ButtonBackground", 687800832);
				public static SettingList titledWindowButtonBorder = GlobalBorderSettings.get(titledWindowButton, true, false, -1761673216, 0.1f);
				public static Setting titledWindowButtonSize = new Setting(titledWindowButton, Mode.DOUBLE, "SizeOffset", new SettingValue(2.4, 0, 5, 0.1), "How big the background/click area is");
			public static Setting titledWindowSlider = new Setting(titledWindow, Mode.LABEL, "Slider", true, "Window slider settings");
				public static Setting titledWindowSliderText = new Setting(titledWindowSlider, Mode.COLOR, "SliderText", -1);
				public static Setting titledWindowSliderBackground = new Setting(titledWindowSlider, Mode.COLOR, "SliderBackground", -5636096);
			public static Setting titledWindowTextField = new Setting(titledWindow, Mode.LABEL, "TextField", true, "Window text field settings");
				public static Setting titledWindowTextFieldText = new Setting(titledWindowTextField, Mode.COLOR, "Text", -4605511, "Text color");
				public static Setting titledWindowTextFieldName = new Setting(titledWindowTextField, Mode.COLOR, "Name", -1, "Name color");
				public static Setting titledWindowTextFieldBackground = new Setting(titledWindowTextField, Mode.COLOR, "Background", 687800320, "Background color");
				public static SettingList titledWindowTextFieldBorder = GlobalBorderSettings.get(titledWindowTextField, true, false, -1761673214, 0.3f);
			public static Setting titledWindowOther = new Setting(titledWindow, Mode.LABEL, "Other", true, "Other settings for specific windows");
				public static Setting titledWindowOtherPickerSelected = new Setting(titledWindowOther, Mode.COLOR, "PickerSelected", 1610547200, "Picker selected background");


		public static Setting guiSettingValues = new Setting(guiSettings, Mode.LABEL, "Values", true, "GUI functionality values");
			public static Setting showKeybind = new Setting(guiSettingValues, Mode.BOOLEAN, "ShowKeybind", true, "Shows the Keybind selector for every module");
			public static Setting showHidden = new Setting(guiSettingValues, Mode.BOOLEAN, "ShowHidden", true, "Shows the Hidden option for every module");
			public static Setting showReset = new Setting(guiSettingValues, Mode.BOOLEAN, "ShowReset", false, "Shows the reset all default settings option for every module");
			public static Setting scale = new Setting(guiSettingValues, Mode.DOUBLE, "Scale", new SettingValue(2.5, 1, 5, 0.005), "How much more to scale it than default", "Higher = bigger");
			public static Setting width = new Setting(guiSettingValues, Mode.DOUBLE, "Width", new SettingValue(75, 30, 150, 0.1), "Gui node width");
			public static Setting height = new Setting(guiSettingValues, Mode.DOUBLE, "Height", new SettingValue(12.5, 5, 40, 0.1), "Gui node height");
			public static Setting scrollAmount = new Setting(guiSettingValues, Mode.INTEGER, "ScrollAmount", new SettingValue(15, 1, 50, 1), "How many times to scroll with one wheel scroll");
			public static Setting borderSize = new Setting(guiSettingValues, Mode.DOUBLE, "Border size", new SettingValue(0.4, 0.1, 5, 0.1), "The size of the border in the node");
			public static Setting groupScale = new Setting(guiSettingValues, Mode.DOUBLE, "Group scale", new SettingValue(1.25, 0.5, 3, 0.05), "The group text scale");
			public static Setting extendMove = new Setting(guiSettingValues, Mode.INTEGER, "Extend Move", new SettingValue(4, 0, 25, 1), "How much to move in x coordinates when parent is extended");
			public static Setting gapBorderSize = new Setting(guiSettingValues, Mode.DOUBLE, "GapBorderSize", new SettingValue(0.4, 0, 10, 0.1), "Border size of the gaps in the module fields");
			public static Setting rainbowBorder = new Setting(guiSettingValues, Mode.BOOLEAN, "RainbowBorder", true, "Makes the side borders rainbowing");
				public static Setting rainbowBorderAlpha = new Setting(rainbowBorder, Mode.INTEGER, "Alpha", new SettingValue(180, 0, 255, 1), "Transparency");
				public static Setting rainbowBorderGroupFactor = new Setting(rainbowBorder, Mode.INTEGER, "GroupFactor", new SettingValue(10, 0, 100, 1), "Makes it wave by groups");
				public static Setting rainbowBorderSpeed = new Setting(rainbowBorder, Mode.INTEGER, "Speed", new SettingValue(20, 1, 100, 1), "Rainbow speed");
			public static Setting bypassLimits = new Setting(guiSettingValues, Mode.BOOLEAN, "BypassLimits", false, "EXTREME WARNING!!!!", "This will bypass all setting limits for min and max values", "If you go past those values you will 100% face issues", "Use at your own risk!");
			public static Setting guiShowIds = new Setting(guiSettingValues, Mode.BOOLEAN, "ShowIDS", false, "Shows the ID's in the description", "So you know what id to use with set command");
			public static Setting guiKeepInBounds = new Setting(guiSettingValues, Mode.BOOLEAN, "KeepInBounds", true, "Keeps the GUI groups inside the screen bounds", "By preventing them from going off bounds when dragged or size changed");
		public static Setting guiWindows = new Setting(guiSettings, Mode.LABEL, "Windows", true, "Always enabled windows");
			public static Setting contactsWindow = new Setting(guiWindows, Mode.BOOLEAN, "Contacts", true);
			public static Setting changelogWindow = new Setting(guiWindows, Mode.BOOLEAN, "Changelog", true);
			public static Setting tipsWindow = new Setting(guiWindows, Mode.BOOLEAN, "Tips", true);


	public static Setting font = new Setting(Mode.LABEL, "Font", true, "Custom font settings for 2D and 3D rendering");
		public static Setting font2d = new Setting(font, Mode.LABEL, "2D", true, "This font is used for 2D rendering in GUI and HUD");
			public static Setting font2dName = new Setting(font2d, "Name", "Helvetica");
				public static Setting font2dNameShowSystem = new Setting(font2dName, Mode.BOOLEAN, "ShowSystem", false, "Shows the system fonts too rather than just built-in");
			public static Setting font2dSize = new Setting(font2d, Mode.DOUBLE, "Size", new SettingValue(8, 4, 20, 0.1));
			public static Setting font2dType = new Setting(font2d, "Type", "Plain", new String[]{"Plain"}, new String[]{"Bold"}, new String[]{"Italic"});
			public static Setting font2dShadow = new Setting(font2d, Mode.BOOLEAN, "Shadow", true, "Font shadow settings");
				public static Setting font2dShadowOffset = new Setting(font2dShadow, Mode.INTEGER, "Offset", new SettingValue(1, 1, 5), "Size or offset of the shadow");
				public static Setting font2dShadowColor = new Setting(font2dShadow, Mode.COLOR, "Color", -16777216, "Shadow color");
			public static Setting font2dGap = new Setting(font2d, Mode.INTEGER, "Gap", new SettingValue(0, -1, 5), "Added character gap");
			public static Setting font2dYAdd = new Setting(font2d, Mode.INTEGER, "YAdd", new SettingValue(-2, -25, 25), "Changes the rendered y position");
		public static Setting font3d = new Setting(font, Mode.LABEL, "3D", true, "This font is used for rendering text in 3D space", "Used in modules like NameTags");
			public static Setting font3dName = new Setting(font3d, "Name", "Helvetica");
				public static Setting font3dNameShowSystem = new Setting(font3dName, Mode.BOOLEAN, "ShowSystem", false, "Shows the system fonts too rather than just built-in");
			public static Setting font3dType = new Setting(font3d, "Type", "Plain", new String[]{"Plain"}, new String[]{"Bold"}, new String[]{"Italic"});
			public static Setting font3dShadow = new Setting(font3d, Mode.BOOLEAN, "Shadow", true, "Font shadow settings");
				public static Setting font3dShadowOffset = new Setting(font3dShadow, Mode.INTEGER, "Offset", new SettingValue(2, 1, 10), "Size or offset of the shadow");
				public static Setting font3dShadowColor = new Setting(font3dShadow, Mode.COLOR, "Color", 1929379840, "Shadow color");
			public static Setting font3dGap = new Setting(font3d, Mode.INTEGER, "Gap", new SettingValue(0, -1, 5), "Added character gap");


	public static Setting games = new Setting(Mode.LABEL, "Games", true, "Fun games to play");
		public static Setting snakeGame = new Setting(games, Mode.BOOLEAN, "Snake", false, false);
		public static Setting tetrisGame = new Setting(games, Mode.BOOLEAN, "Tetris", false, false);
		public static Setting mineSweeperGame = new Setting(games, Mode.BOOLEAN, "MineSweeper", false, false);
			public static Setting mineSweeperGameBoardSize = new Setting(mineSweeperGame, Mode.INTEGER, "BoardSize", new SettingValue(15, 5, 30, 1), "The size of the game board, default is 15x15", "Start a new game for changes to take effect");
			public static Setting mineSweeperGameBombs = new Setting(mineSweeperGame, Mode.INTEGER, "Bombs", new SettingValue(25, 1, 200, 1), "How many bombs the game will conmtain", "Start a new game for changes to take effect");


	public static Setting tools = new Setting(Mode.LABEL, "Tools", true, "Tool windows");
		public static Setting reflectorTool = new Setting(tools, Mode.BOOLEAN, "Reflector", false, false, "Tool that shows all loaded classes in the classpath", "And shows all fields and methods for selected class", "Also allows modification of static fields / methods");
		public static Setting textureEditorTool = new Setting(tools, Mode.BOOLEAN, "TextureEditor", false, false, "Tool that allows you to view and edit all textures loaded in the game", "For example you can disable some texture or edit the image texture of others", "The possibilities with this are unlimited", "To remove modified textures enable Show selected then right click on the item");

	public static Setting client = new Setting(Mode.LABEL, "Client", true, "Other client settings");
		public static Setting prefix = new Setting(client, Mode.TEXT, "Prefix", "++", "The prefix for commands");
		public static Setting portalGui = new Setting(client, Mode.BOOLEAN, "PortalGui", true, "Allows you to open guis in portals");
		public static Setting reconnectButton = new Setting(client, Mode.BOOLEAN, "ReconnectButton", true, "Adds a reconnect button to disconnected screen");
		public static Setting clientMessages = new Setting(client, Mode.LABEL, "ClientMessages", true, "Colors and settings for client messages");
			public static Setting clientMessageBracketsColor = new Setting(clientMessages, Mode.TEXT, "Bracket", "DARK_GRAY", "Text color for the brackets in [" + Mod.NAME + "]", "This uses the Formatting color codes because", "im not going to mess with the default font renderer just for this", "Example values are: WHITE, RED, YELLOW, GRAY, GREEN etc...");
			public static Setting clientMessageNameColor = new Setting(clientMessages, Mode.TEXT, "Name", "DARK_AQUA", "Text color for " + Mod.NAME + " inside the brackets", "This uses the Formatting color codes because", "im not going to mess with the default font renderer just for this", "Example values are: WHITE, RED, YELLOW, GRAY, GREEN etc...");
			public static Setting clientMessageColor = new Setting(clientMessages, Mode.TEXT, "Message", "GRAY", "Text color for the message that isn't red (warning)", "This uses the Formatting color codes because", "im not going to mess with the default font renderer just for this", "Example values are: WHITE, RED, YELLOW, GRAY, GREEN etc...");
			public static Setting clientMessageIncludeModule = new Setting(clientMessages, Mode.BOOLEAN, "IncludeModule", true, "Includes the module name next to " + Mod.NAME, "that the message is sent from");
		public static Setting entityTypeColors = new Setting(client, Mode.LABEL, "EntityColors", true, "Colors for different entity types used in rendering");
			public static Setting entityTypeColorPassive = new Setting(entityTypeColors, Mode.COLOR, "Passive", Color.GREEN.getRGB());
			public static Setting entityTypeColorNeutral = new Setting(entityTypeColors, Mode.COLOR, "Neutral", Color.YELLOW.getRGB());
			public static Setting entityTypeColorHostile = new Setting(entityTypeColors, Mode.COLOR, "Passive", Color.RED.getRGB());
			public static Setting entityTypeColorPlayers = new Setting(entityTypeColors, Mode.COLOR, "Players", Color.PINK.getRGB());
			public static Setting entityTypeColorItems = new Setting(entityTypeColors, Mode.COLOR, "Items", Color.GRAY.getRGB());
			public static Setting entityTypeColorProjectiles = new Setting(entityTypeColors, Mode.COLOR, "Projectiles", Color.BLACK.getRGB());
			public static Setting entityTypeColorsDefault = new Setting(entityTypeColors, Mode.COLOR, "Default", Color.WHITE.getRGB(), "Default value if none above apply");

	public static Setting guiSearch = new Setting(Mode.TEXT, "Find", "", "Search modules from the GUI");

	static {
		bypassLimits.addValueChangedListener(new Setting.ValueChangedListener(null, false) {
			public void valueChanged() {
				if (bypassLimits.bool()) {
					ClientUtils.sendMessage("Enabled bypasslimits, i hope you know what you are doing!!", Formatting.RED, "GUI");
				} else {
					ClientUtils.sendMessage("Disabled bypasslimits, whew! Note: the bypassed values will still stay, so there might still be issues. Reset them manually", Formatting.RED, "GUI");
				}
			}
		});

		initFontListeners(Gui.fontRenderer, font2dNameShowSystem, font2dName, new Setting[]{
				font2dName, font2dSize, font2dType, font2dShadow, font2dShadowOffset, font2dShadowColor, font2dGap, font2dYAdd
		});

		initFontListeners(Renderer2DIn3D.fontRenderer, font3dNameShowSystem, font3dName, new Setting[]{
				font3dName, font3dType, font3dShadow, font3dShadowOffset, font3dShadowColor, font3dGap
		});
	}

	private static void initFontListeners(SierraFontRenderer fontRenderer, Setting showSystem, Setting name, Setting[] addListeners) {
		showSystem.addValueChangedListener(new Setting.ValueChangedListener(null, false) {
			public void valueChanged() {
				name.modes.clear();
				name.modeDescriptions.clear();

				for (String[] mode : getFonts()) {
					name.modes.add(mode[0]);

					ArrayList<String> descriptions = new ArrayList<>(Arrays.asList(mode).subList(1, mode.length));
					name.modeDescriptions.add(descriptions);
				}
			}
		});
		showSystem.listeners.get(0).valueChanged();

		Setting.ValueChangedListener listener = new Setting.ValueChangedListener(null, false) {
			public void valueChanged() {
				fontRenderer.clearHashes();
			}
		};

		for (Setting addListener : addListeners) {
			addListener.addValueChangedListener(listener);
		}
	}

	private static String[][] getFonts() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = ge.getAvailableFontFamilyNames();

		//Create a String[][] object to store the font names
		java.util.List<String[]> validFonts = new ArrayList<>();

		//Populate the fontArray with font names
		for (String s : fontNames) {
			Font font = new Font(s, Font.PLAIN, 12);
			if (!SierraFontRenderer.builtInFonts.contains(font.getFamily()) && !ClientSettings.font2dNameShowSystem.bool()) {
				continue;
			}

			if (font.canDisplay('a')) {
				validFonts.add(new String[]{s});
			}
		}

		//Convert list to array
		String[][] stringArray = new String[validFonts.size()][];
		for (int i = 0; i < validFonts.size(); i++) {
			stringArray[i] = validFonts.get(i);
		}

		return stringArray;
	}
}