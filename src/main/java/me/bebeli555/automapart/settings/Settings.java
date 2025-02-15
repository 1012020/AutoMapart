package me.bebeli555.automapart.settings;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.command.commands.DevCommand;
import me.bebeli555.automapart.gui.Group;
import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.hud.components.InfoClusterComponent;
import me.bebeli555.automapart.utils.objects.Timer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Settings extends Mod {
	public static Timer lastSave = new Timer();
	public static String path = mc.runDirectory.getPath() + "/" + Mod.NAME;
	public static File settings = new File(path + "/Settings");

	//Readme
	static {
		try {
			new File(path).mkdir();
			File file = new File(path + "/README.txt");
			file.delete();
			file.createNewFile();

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
			bw.write("Welcome to " + Mod.NAME + " directory!");
			bw.newLine();
			bw.write("This is the land of important files that make your client run correctly!");
			bw.newLine();
			bw.newLine();
			bw.write("Only modify the files that are meant to be modified");
			bw.newLine();
			bw.write("If you modify something else and the client stops working");
			bw.newLine();
			bw.write("There will be no support given to you! :'(");
			bw.newLine();
			bw.newLine();
			bw.write("Have a good day/night or anything really!");
			bw.newLine();
			bw.write("Just having a great time is always great");
			bw.newLine();
			bw.write("Remember to let go and enjoy the moments at hand");

			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves the settings from the GUI to a file located at .minecraft/Sierra/Settings
	 */
	public static void saveSettings() {
		if (!lastSave.hasPassed(1000)) {
			return;
		}
		lastSave.reset();
		
		new Thread(() -> {
			try {
				String content = "";
				char newLine = '\n';

				for (GuiNode node : GuiNode.all) {
					if (node.setting != null && !node.setting.saveSetting) {
						continue;
					}

					content += node.id + "=";
					if (!node.isTypeable && node.modes.size() == 0 && !node.isColor && !node.isPicker) {
						content += "" + node.toggled;
					} else {
						content += "" + node.stringValue;
					}

					content += newLine;
				}

				//Also save the group coordinates
				for (Group group : Group.values()) {
					content += "Group88" + group.name + "=" + group.x + "," + group.y + "," + group.extended;
					content += newLine;
				}

				//And save window positions if needed
				for (TitledWindow window : TitledWindow.list) {
					if (window.loadDefaults) {
						content += "Window88" + window.title + "=" + window.x + "," + window.y;
						content += newLine;
					}
				}

				//Save HUD positions
				for (HudComponent component : HudComponent.components) {
					if (component instanceof InfoClusterComponent) {
						String c = "";
						for (String key : component.textAdds.keySet()) {
							Double[] value = component.textAdds.get(key);
							if (value != null) {
								c += key + ":" + value[0] + ":" + value[1] + ",";
							}
						}

						if (c.endsWith(",")) {
							c = c.substring(0, c.length() - 1);
						}

						content += "HUD88TextAdds" + "=" + c;
						content += newLine;
					}

					content += "HUD88" + component.name + "=" + component.corner.id + "," + component.getxAdd() + "," + component.getyAdd();
					content += newLine;
				}

				if (DevCommand.printNextSettings) {
					DevCommand.printNextSettings = false;
					System.out.println(content);
				}

				//Compress
				settings.createNewFile();
				Files.writeString(settings.toPath(), content);
			} catch (Exception e) {
				System.out.println(NAME + " - Error saving settings");
				e.printStackTrace();
			}
		}).start();
	}
	
	/**
	 * Loads the settings saved in the file
	 */
	public static void loadSettings() {
		try {
			if (!settings.exists()) {
				return;
			}

			byte[] bytes = Files.readAllBytes(settings.toPath());
			String content = new String(bytes, StandardCharsets.UTF_8);

			outer: for (String line : content.split("\n")) {
				String[] split = line.split("=");
				String id = split[0];
				String value;
				try {
					value = split[1];
				} catch (IndexOutOfBoundsException e) {
					value = "";
				}

				//If setting is group then do this trick.
				if (id.startsWith("Group88")) {
					String name = id.replace("Group88", "");
					double x = Double.parseDouble(value.split(",")[0]);
					double y = Double.parseDouble(value.split(",")[1]);
					boolean extended = Boolean.parseBoolean(value.split(",")[2]);

					for (Group group : Group.values()) {
						if (group.name.equals(name)) {
							group.x = x;
							group.y = y;
							group.extended = extended;
						}
					}

					continue;
				}

				//Load window enabled values
				if (id.startsWith("Window88")) {
					String name = id.replace("Window88", "");
					for (TitledWindow window : TitledWindow.list) {
						if (window.title.equals(name)) {
							String[] splitWind = value.split(",");
							int x = Integer.parseInt(splitWind[0]);
							int y = Integer.parseInt(splitWind[1]);
							if (x != 0 || y != 0) {
								window.enableX = x;
								window.enableY = y;
							}
						}
					}

					continue;
				}

				//Load HUD text adds
				if (id.startsWith("HUD88TextAdds")) {
					String[] set = value.split(",");
					for (String values : set) {
						try {
							String[] splitValue = values.split(":");
							Double[] d = new Double[]{Double.parseDouble(splitValue[1]), Double.parseDouble(splitValue[2])};
							InfoClusterComponent.INSTANCE.textAdds.put(splitValue[0], d);
						} catch (IndexOutOfBoundsException ignored) {}
					}

					continue;
				}

				//Load HUD positions
				if (id.startsWith("HUD88")) {
					String name = id.replace("HUD88", "");
					for (HudComponent component : HudComponent.components) {
						if (component.name.equals(name)) {
							String[] splitHud = value.split(",");
							component.corner = HudComponent.HudCorner.getCornerFromId(Integer.parseInt(splitHud[0]));
							component.setxAdd(Double.parseDouble(splitHud[1]));
							component.setyAdd(Double.parseDouble(splitHud[2]));
							component.setFromSettings = true;
							break;
						}
					}

					continue;
				}

				GuiNode node = getGuiNodeFromId(id);
				if (node == null) {
					continue;
				}

				//Don't load setting if mod has set disableOnStart
				for (Mod mod : Mod.modules) {
					if (mod.name.equals(id)) {
						if (mod.disableOnStart) {
							continue outer;
						}
					}
				}

				if (isBoolean(value)) {
					node.toggled = Boolean.parseBoolean(value);
					try {
						node.setting.setValue(node.toggled);
					} catch (Exception ignored) {}

					for (Mod module : modules) {
						if (module.name.equals(id)) {
							if (node.toggled) {
								try {
									module.enable();
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else if (module.isOn()) {
								try {
									module.disable();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				} else {
					node.stringValue = value;
					try {
						node.setSetting();
					} catch (NullPointerException e) {
						//Ignore exception bcs its probably caused by the keybind which doesnt have a setting only the node
					}
				}
			}
		} catch (Exception e) {
			System.out.println(NAME + " - Error loading settings");
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks if the setting with given ID is toggled
	 */
	public static boolean isOn(String id) {
		return getGuiNodeFromId(id).toggled;
	}
	
	/**
	 * @return String value of GuiNode with given ID
	 */
	public static String getStringValue(String id) {
		return getGuiNodeFromId(id).stringValue;
	}
	
	/**
	 * String value of this setting turned into integer
	 */
	public static int getIntValue(String id) {
		return Integer.parseInt(getGuiNodeFromId(id).stringValue);
	}
	
	/**
	 * String value of this setting turned into double
	 */
	public static double getDoubleValue(String id) {
		return Double.parseDouble(getGuiNodeFromId(id).stringValue);
	}
	
	/**
	 * Get GuiNode with given ID
	 */
	public static GuiNode getGuiNodeFromId(String id) {
		return GuiNode.nodeHash.get(id.toLowerCase());
	}
	
	//Checks if string is boolean
	public static boolean isBoolean(String string) {
		return "true".equals(string) || "false".equals(string);
	}
}
