package me.bebeli555.automapart;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandListener;
import me.bebeli555.automapart.events.game.ClientTickEvent;
import me.bebeli555.automapart.events.game.FirstPlayerInitializationEvent;
import me.bebeli555.automapart.events.game.GamePostInitEvent;
import me.bebeli555.automapart.gui.Group;
import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.windows.tools.TextureEditorTool;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.hud.HudEditor;
import me.bebeli555.automapart.hud.HudRenderer;
import me.bebeli555.automapart.hud.components.ArrayListComponent;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.mods.world.AutoMapart;
import me.bebeli555.automapart.settings.Keybind;
import me.bebeli555.automapart.settings.SetGuiNodes;
import me.bebeli555.automapart.settings.Settings;
import me.bebeli555.automapart.utils.ClientUtils;
import me.bebeli555.automapart.utils.EatingUtil;
import me.bebeli555.automapart.utils.Utils;
import me.bebeli555.automapart.utils.font.SierraFontRenderer;
import me.bebeli555.automapart.utils.input.Keyboard;
import me.bebeli555.automapart.utils.input.Mouse;
import me.bebeli555.automapart.utils.objects.CustomEventBus;
import me.bebeli555.automapart.utils.objects.EnderchestMemory;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Formatting;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mod extends Utils implements ClientModInitializer {
	public static final String MOD_ID = "automapart";
	public static final String NAME = "AutoMapart";
    public static final String VERSION = "1.02";

    public static final CustomEventBus EVENT_BUS = new CustomEventBus();

    public static List<Mod> modules = new ArrayList<>();
	public static List<Runnable> clientRunnables = new ArrayList<>();
	public static boolean modInitializationDone, initializedPlayer;
	public static long playerJoinWorld = System.currentTimeMillis();

	public String name = "";
	public String[] description;
	public Group group;
	private boolean toggled, hiddenOn;
	public boolean defaultOn, defaultHidden;
	public boolean autoSubscribe = true;
	public boolean disableOnStart;
	private GuiNode guiNode, hiddenNode;
	public int renderNumber = -1;

    public Mod(Group group, String name, String... description) {
		if (name.contains(" ")) {
			throw new RuntimeException("Module name cannot contain spaces");
		}

    	this.group = group;
    	this.name = name;
    	this.description = description;
    	modules.add(this);
    }
    
    public Mod(Group group) {
    	this.group = group;
    	modules.add(this);
    }

	public Mod() {}

	public void onEnabled(){}
	public void onDisabled(){}
	public void onToggled() {}
	public void onPostInit(){}

	@Subscribe
	public void tickEvent(ClientTickEvent e) {
		if (mc.player != null && !initializedPlayer) {
			initializedPlayer = true;
			playerJoinWorld = System.currentTimeMillis();
			Mod.EVENT_BUS.post(new FirstPlayerInitializationEvent());

			//First use info message
			if (!Settings.settings.exists()) {
				Formatting text = Formatting.byName(ClientSettings.clientMessageColor.string());
				Formatting name = Formatting.byName(ClientSettings.clientMessageNameColor.string());

				ClientUtils.sendMessage("Welcome to " + name + Mod.NAME + text + " version " + name + Mod.VERSION, text);
				ClientUtils.sendMessage("You can open the GUI by typing " + name + ClientSettings.prefix.string() + "gui" + text + " on chat", text);
				ClientUtils.sendMessage("Then set a keybind for it in " + name + "Client" + text + " -> " + name + "Keybind", text);
				Settings.saveSettings();
			}
		}

		for (Runnable runnable : clientRunnables) {
			try {
				runnable.run();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		clientRunnables.clear();
	}

	/**
	 * Runs the given runnable on the client thread on the next tick
	 */
	public void run(Runnable runnable) {
		clientRunnables.add(runnable);
	}

	@Override
	public void onInitializeClient() {
		TextureEditorTool.loadSavedTextures();

		new File(Settings.path + "/data").mkdir();
		SierraFontRenderer.registerFonts();

		//Register classes to event buses
		List<Object> subscribers = Arrays.asList(
				new CommandListener(),
				new Keybind(),
				new EatingUtil(),
				new Mouse(),
				new Keyboard(),
				new HudRenderer(),
				new EnderchestMemory()
		);
		subscribers.forEach(subscriber -> Mod.EVENT_BUS.register(subscriber));

		//Init mods
		initMods();
		ClientSettings.width.asDouble();
	}

	@Subscribe
	public void onPostInit(GamePostInitEvent event) {
		if (modInitializationDone) {
			return;
		}

		//Initialize stuff after minecraftclient constructor has finished
		new File(Settings.path).mkdir();
		SetGuiNodes.setGuiNodes();
		SetGuiNodes.setDefaults();
		TitledWindow.initWindows();
		Settings.loadSettings();
		Keybind.setKeybinds();
		Command.initCommands();
		HudComponent.setDefaults();

		for (Mod module : modules) {
			module.onPostInit();
		}

		modInitializationDone = true;
	}

	/**
	 * Enables this module
	 */
    public void enable() {
    	if (autoSubscribe) {
    		Mod.EVENT_BUS.register(this);
    	}

    	getGuiNode().toggled = true;

		if (!ArrayListComponent.arraylist.contains(this)) {
			ArrayListComponent.arraylist.add(this);
		}

		this.toggled = true;
		this.onEnabled();
		this.onToggled();
    }

	/**
	 * Disables this module
	 */
	public void disable() {
    	if (autoSubscribe) {
    		Mod.EVENT_BUS.unregister(this);
    	}

    	getGuiNode().toggled = false;
		ArrayListComponent.arraylist.remove(this);
		this.toggled = false;
		this.hiddenOn = false;
		this.onDisabled();
		this.onToggled();
    }

	/**
	 * Toggles this module so if off will enable or if on will disable
	 */
	public void toggle() {
    	if (toggled) {
    		disable();
    	} else {
    		enable();
    	}
    }

	/**
	 * Checks if the module is current enabled
	 */
	public boolean isOn() {
		return toggled || hiddenOn;
	}

	/**
     * Sets the module on but doesn't show it in gui or arraylist or anything
     * This can be used by other modules to turn this module on
     */
    public void setHiddenOn(boolean value) {
    	this.hiddenOn = value;

		if (this.hiddenOn) {
			if (this.autoSubscribe) Mod.EVENT_BUS.register(this);
			onEnabled();
		} else {
			if (this.autoSubscribe) Mod.EVENT_BUS.unregister(this);
			onDisabled();
		}
    }

	/**
	 * Gets the GuiNode of this module
	 */
    public GuiNode getGuiNode() {
    	if (guiNode == null) {
    		guiNode = Settings.getGuiNodeFromId(name);
		}

		return guiNode;
	}

	/**
	 * Checks if this module is set to be hidden
	 */
	public boolean isHidden() {
    	if (hiddenOn) {
    		return true;
    	}
    	
    	if (hiddenNode == null) {
        	hiddenNode = Settings.getGuiNodeFromId(name + "Hidden");
    	}
    	
    	return hiddenNode.toggled;
    }

	/**
	 * Sets the render number back to -1 so its no longer displayed
	 */
	public void clearRenderNumber() {
		this.renderNumber = -1;
	}

	/**
	 * Sends clientsided message using the module name with given color
	 */
	public void sendMessage(String text, Formatting color) {
		if (mc.player == null) {
			System.out.println(text);
		} else {
			ClientUtils.sendMessage(text, color, name);
		}
	}

	/**
	 * Sends clientsided message using the module name with default color
	 */
	public void sendMessage(String text) {
		ClientUtils.sendMessage(text, null, name);
	}

	/**
	 * Initializes the module classes
	 */
	public void initMods() {
		new AutoMapart();

		//Sort the modules list from A to Z
		List<String> names = new ArrayList<>();
		for (Mod module : modules) {
			names.add(module.name);
		}

		String[] sortedNames = new String[names.size()];
		sortedNames = names.toArray(sortedNames);
		Arrays.sort(sortedNames);

		ArrayList<Mod> temp = new ArrayList<>();
		for (String name : sortedNames) {
			for (Mod module : modules) {
				if (module.name.equals(name)) {
					temp.add(module);
					break;
				}
			}
		}

		modules = temp;

		//Gui
		new HudEditor();
		new ClientSettings();
	}

	/**
	 * Finds a module with this name
	 */
	public static Mod findMod(String name) {
		return modules.stream().filter(module -> module.name.equals(name)).findFirst().orElse(null);
	}
}
