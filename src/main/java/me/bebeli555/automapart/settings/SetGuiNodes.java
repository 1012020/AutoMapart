package me.bebeli555.automapart.settings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Group;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.utils.ClientUtils;

public class SetGuiNodes {
	
	//Sets the GuiNodes by looping through the modules
	//Then checking all the variables in the class and if it's a Setting variable add that as a GuiNode
	public static void setGuiNodes() {
		try {
			List<Mod> temp = new ArrayList<>(Mod.modules);
			for (Mod module : temp) {
				GuiNode mainNode;
				Setting s = null;

				if (module.name.isEmpty()) {
					mainNode = new GuiNode(true);
					mainNode.group = module.group;
				} else {
					mainNode = new GuiNode();
					mainNode.group = module.group;
					mainNode.name = module.name;
					mainNode.description = module.description;
					mainNode.isVisible = true;
					mainNode.setId();
					s = new Setting(Mode.BOOLEAN, mainNode.name, false, mainNode.description);
					mainNode.setting = s;
				}

				for (Field field : module.getClass().getFields()) {
					Class<?> settingType = Setting.class;
					Class<?> settingListType = SettingList.class;

					if (field.getType().isAssignableFrom(settingType) || field.getType().isAssignableFrom(settingListType)) {
						List<Setting> settings = new ArrayList<>();
						if (field.getType().isAssignableFrom(settingType)) {
							settings.add((Setting)field.get(module));
						} else {
							SettingList settingList = (SettingList)field.get(module);
							settings.addAll(settingList.list);
						}

						for (Setting setting : settings) {
							if (!mainNode.id.isEmpty()) {
								setting.id = mainNode.id + setting.id;
							}

							GuiNode node = new GuiNode();
							node.name = setting.name;
							node.description = setting.description;
							node.defaultValue = String.valueOf(setting.defaultValue);
							node.group = mainNode.group;
							node.modeName = setting.modeName;
							node.setting = setting;
							node.instaType = setting.id.equals("Find");

							if (setting.parent != null) {
								GuiNode p = Settings.getGuiNodeFromId(setting.parent.id);
								node.parent = p;
								p.parentedNodes.add(node);
							} else if (!mainNode.id.isEmpty()) {
								node.parent = mainNode;
								mainNode.parentedNodes.add(node);
							} else {
								node.isVisible = true;
							}

							if (setting.mode == Mode.PICKER) {
								node.isPicker = true;

								node.addClickListener(new GuiNode.ClickListener(){
									public void clicked() {
										if (!setting.getPickerWindow().isToggled()) {
											setting.getPickerWindow().enable(setting, Gui.lastMouseX, Gui.lastMouseY);
										}
									}
								});
							} else if (setting.mode == Mode.COLOR) {
								node.isColor = true;
							} else if (setting.mode == Mode.TEXT) {
								node.isTypeable = true;
							} else if (setting.mode == Mode.INTEGER) {
								node.isTypeable = true;
								node.onlyNumbers = true;
							} else if (setting.mode == Mode.DOUBLE) {
								node.isTypeable = true;
								node.onlyNumbers = true;
								node.acceptDoubleValues = true;
							} else if (setting.mode == Mode.LABEL) {
								node.isLabel = true;
							} else if (setting.modes.size() != 0) {
								node.modes = setting.modes;
								node.modeDescriptions = setting.modeDescriptions;
							} else if (setting.mode == Mode.BOOLEAN) {
								try {
									node.toggled = (Boolean)setting.defaultValue;
								} catch (Exception ignored) {}
							} else if (setting.mode == Mode.KEYBIND) {
								node.isTypeable = true;
								node.isKeybind = true;
							} else if (setting.mode == Mode.TEXT_ARRAY) {
								node.isTextArray = true;
								node.isTypeable = true;
							}

							node.stringValue = setting.string();
							node.setId();

							//Delete this if there is already other one with same id
							if (GuiNode.all.stream().filter(n -> n.id.equals(node.id)).toList().size() > 1) {
								GuiNode.all.remove(node);
							}
						}
					}
				}

				//Keybind setting and node
				GuiNode keybind = new GuiNode();
				keybind.isVisible = true;
				if (s != null) {
					mainNode.parentedNodes.add(keybind);
					keybind.description = new String[]{"Keybind for " + mainNode.name};
					keybind.parent = mainNode;
					keybind.isVisible = false;
				} else {
					String add = module.group == Group.CLIENT ? " GUI" : "";
					keybind.description = new String[]{"Keybind for " + module.group.name + add};
				}
				keybind.group = module.group;
				keybind.isTypeable = true;
				keybind.isKeybind = true;
				keybind.name = "Keybind";
				keybind.setId();

				//Hidden setting
				if (s == null) {
					continue;
				}

				GuiNode hidden = new GuiNode();
				hidden.isVisible = true;
				mainNode.parentedNodes.add(hidden);
				hidden.description = new String[]{"Hides the module in the HUD arraylist"};
				hidden.parent = mainNode;
				hidden.isVisible = false;
				hidden.group = module.group;
				hidden.name = "Hidden";
				hidden.setId();

				GuiNode resetDefaults = new GuiNode();
				resetDefaults.isVisible = false;
				mainNode.parentedNodes.add(resetDefaults);
				resetDefaults.description = new String[]{"Click this to reset all default values for this module"};
				resetDefaults.parent = mainNode;
				resetDefaults.group = module.group;
				resetDefaults.name = "ResetDefaults";
				resetDefaults.setId();

				resetDefaults.addClickListener(new GuiNode.ClickListener() {
					@Override
					public void clicked() {
						resetDefaults.toggled = false;
						for (GuiNode node : GuiNode.all) {
							if (node.id.startsWith(mainNode.id)) {
								node.setDefaultValue();
							}
						}

						ClientUtils.sendMessage("Reset default settings for " + mainNode.name);
					}
				});
			}

			GuiNode.setNodeHash();
		} catch (Exception e) {
			System.out.println(Mod.NAME + " - Exception setting gui nodes");
			e.printStackTrace();
		}
	}

	//Set default things if no setting file
	public static void setDefaults() {
		//Set defaults if no settings file
		if (!Settings.settings.exists()) {
			for (Mod module : Mod.modules) {
				if (module.defaultOn) {
					Settings.getGuiNodeFromId(module.name).click();
				}
				
				if (module.defaultHidden) {
					Settings.getGuiNodeFromId(module.name + "Hidden").click();
				}
			}
		}
	}
}
