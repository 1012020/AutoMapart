package me.bebeli555.automapart.hud;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Group;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.hud.components.*;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.settings.Settings;
import me.bebeli555.automapart.utils.objects.ClientStatus;

public class HudEditor extends Mod {
	public static HudEditor INSTANCE;

	public static Setting color = new Setting(Mode.BOOLEAN, "Color", true, "When disabled it stops rendering the background color", "So you can toggle this to look at the components", "better if the background is blocking the view");
		public static Setting colorColor = new Setting(color, Mode.COLOR, "Color", 2013265920, "Color for the background of HUD components");
	public static Setting backgroundColor = new Setting(Mode.COLOR, "Background", 520093695, "HudEditor background color");
	public static Setting nodeBackground = new Setting(Mode.COLOR, "NodeBackground", 1509949696, "Background ADD color of the rendered nodes");
	public static Setting extend = new Setting(Mode.INTEGER, "Extend", new SettingValue(0, 0, 10, 1), "How much to extend the added background selector");

	public static HudEditorGui hudEditorGui = new HudEditorGui();
	public double clientX, clientY;
	public static boolean closedProgrammatically;

	public HudEditor() {
		super(Group.CLIENT, "HudEditor", "Change the position of the HUD components", "Left mouse: Dragging, Right mouse: Moving corner, Middleclick: Reset to default pos");
		INSTANCE = this;
	}
	
	@Override
	public void onEnabled() {
		if (mc.player == null) {
			disable();
			return;
		}

		Mod.EVENT_BUS.register(hudEditorGui);
		mc.setScreen(hudEditorGui);

		clientX = Group.CLIENT.x;
		clientY = Group.CLIENT.y;

		GuiNode node = Settings.getGuiNodeFromId("HUD");
		if (!node.isExtended) {
			node.extend(true);
		}

		ClientStatus.setStatus("Example status");
	}
	
	@Override
	public void onDisabled() {
		Mod.EVENT_BUS.unregister(hudEditorGui);
		Group.CLIENT.x = clientX;
		Group.CLIENT.y = clientY;

		if (!closedProgrammatically) {
			Gui.openGui = true;
		}

		closedProgrammatically = false;

		GuiNode node = Settings.getGuiNodeFromId("HUD");
		if (node.isExtended) {
			node.extend(false);
		}

		//TODO: new status system needed
		//clearStatus();
	}

	public static void initComponents() {
		new InfoClusterComponent();
		new ArrayListComponent();
		new CoordsComponent();
		new DirectionComponent();
		new LagNotifierComponent();
		new MiniMapComponent();
		new KeyStrokesComponent();
		new ArmorComponent();
		new InventoryViewerComponent();
		new ItemCountsComponent();
		new EntityListComponent();
		new TextRadarComponent();
		new TrafficComponent();
		new PlayerModelComponent();
		new StopWatchComponent();
		new WatermarkComponent();
		new LogoComponent();
		new DevPanelComponent();
	}
}