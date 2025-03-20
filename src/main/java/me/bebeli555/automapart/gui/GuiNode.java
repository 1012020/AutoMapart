package me.bebeli555.automapart.gui;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.windows.windows.other.ColorPickerWindow;
import me.bebeli555.automapart.gui.windows.windows.other.TextArrayWindow;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.utils.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GuiNode extends Mod {
	public static ArrayList<GuiNode> all = new ArrayList<GuiNode>();
	public static HashMap<String, GuiNode> nodeHash = new HashMap<>();

	public GuiNode parent;
	public String name;
	public String id = "";
	public String stringValue = "";
	public String renderValue = "";
	public String defaultValue = "";
	public String modeName = "";
	public boolean isTypeable;
	public boolean onlyNumbers;
	public boolean acceptDoubleValues;
	public boolean isVisible;
	public boolean toggled;
	public boolean isLabel;
	public boolean isKeybind;
	public boolean isExtended;
	public boolean isColor;
	public boolean isPicker;
	public boolean instaType;
	public boolean isTextArray;
	public int lastSliderX;
	public Setting setting;
	public Group group;
	public String[] description;
	public ArrayList<String> modes = new ArrayList<String>();
	public ArrayList<ArrayList<String>> modeDescriptions = new ArrayList<ArrayList<String>>();
	public ArrayList<GuiNode> parentedNodes = new ArrayList<GuiNode>();
	public ArrayList<ClickListener> clickListeners = new ArrayList<ClickListener>();
	public ArrayList<KeyListener> keyListeners = new ArrayList<KeyListener>();
	
	public GuiNode(boolean dontAdd) {}
	
	public GuiNode() {
		all.add(this);
	}

	//Sets the value this can be called as an API function??? ye
	public void setValue(String value) {
		stringValue = value;
		setSetting();
	}

	//Sets the setting with the same ID as this GuiNode
	public void setSetting() {
		try {
			if (isPicker) {
				setting.setValue(stringValue);
				return;
			}

			if (isKeybind) {
				try {
					renderValue = stringValue.isEmpty() ? "" : Keyboard.getKeyName(Integer.parseInt(stringValue));
				} catch (NumberFormatException ignored) {}
			}

			if (isColor) {
				setting.setValue(Integer.parseInt(stringValue));
			} else if (isTypeable || modes.size() != 0) {
				if (acceptDoubleValues) {
					try {
						setting.setValue(Double.parseDouble(stringValue));
					} catch (Exception e) {
						stringValue = "";
						setting.setValue(-1);
					}
				} else if (onlyNumbers) {
					try {
						setting.setValue(Integer.parseInt(stringValue));
					} catch (Exception e) {
						stringValue = "";
						setting.setValue(-1);
					}
				} else if (!isKeybind) {
					setting.setValue(stringValue);
				}
			} else {
				try {
					setting.setValue(toggled);
				} catch (Exception e) {
					//Hidden setting
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean shouldRender() {
		if (isKeybind) {
			return ClientSettings.showKeybind.bool();
		} else if (name.equals("Hidden") && !isTypeable) {
			return ClientSettings.showHidden.bool();
		} else if (name.equals("ResetDefaults")) {
			return ClientSettings.showReset.bool();
		} else {
			return true;
		}
	}

	//Add click listener
	public void addClickListener(ClickListener listener) {
		clickListeners.add(listener);
	}
	
	//Add key listener
	public void addKeyListener(KeyListener listener) {
		keyListeners.add(listener);
	}
	
	//Sets this to the default value
	public void setDefaultValue() {
		if (this.isColor || this.isTypeable || this.modes.size() > 0 || this.isPicker || this.isKeybind) {
			 stringValue = defaultValue;
		} else {
			toggled = Boolean.parseBoolean(defaultValue);
		}

		setSetting();
	}

	public void click() {
		click(-1, -1, -1);
	}

	//Gets called when this node is clicked on the gui
	public void click(int x, int y, int button) {
		if (isLabel) {
			return;
		}
		
		//Mode
		if (modes.size() != 0) {
			extend(false);
			
			try {
				stringValue = modes.get(modes.indexOf(stringValue) + 1);
			} catch (IndexOutOfBoundsException e) {
				stringValue = modes.get(0);
			}
		}
		
		//Toggle
		if (!this.isTypeable && modes.size() == 0) {
			toggled = !toggled;
			
			for (Mod module : modules) {
				if (module.name.equals(name) && module.getGuiNode().id.equals(this.id)) {
					if (toggled) {
						module.enable();
					} else {
						module.disable();
					}
					
					break;
				}
			}
		}

		//Color
		if (this.isColor && !ColorPickerWindow.INSTANCE.isToggled()) {
			ColorPickerWindow.currentNode = this;
			ColorPickerWindow.INSTANCE.enable(x, y);
		}

		//Text array
		if (this.isTextArray && !TextArrayWindow.INSTANCE.isToggled()) {
			TextArrayWindow.currentNode = this;
			TextArrayWindow.INSTANCE.enable(x, y);
		}

		//Notify listeners
		setSetting();
		notifyClickListeners();
	}
	
	/**
	 * Extend this node and reveal all the nodes that parent this node
	 * @param extend if true it will extend it if false it will un extend it
	 */
 	public void extend(boolean extend) {
		isExtended = extend;
 		for (GuiNode node : parentedNodes) {
			if (!node.modeName.isEmpty() && !node.modeName.equals(stringValue)) {
				continue;
			}
 			
 			node.isVisible = extend;
 			
 			//Un extend all the other nodes that parent the extends too
 			if (!extend) {
 	 			for (GuiNode n : all) {
 	 				if (n.id.contains(node.id)) {  	 					
 	 					n.isVisible = false;
 	 				}
 	 			}
 			}
 		}
 	}
 	
	//Sets the ID for this node.
	public void setId() {
		if (this.parent != null) {
			ArrayList<GuiNode> parents = getAllParents();
			
			for (int i = parents.size(); i-- > 0;) {
				this.id += parents.get(i).name;
			}
			
			this.id += name;
		} else {
			this.id = name;
		}
		
		id += modeName;

		setNodeHash();
	}

	public static void setNodeHash() {
		nodeHash = all.stream().collect(Collectors.toMap(n -> n.id.toLowerCase(), Function.identity(), (a, b) -> b, HashMap::new));
	}

	//Get text color
	public int getTextColor() {
		if (isPicker) {
			 return ClientSettings.blockPickerText.asInt();
		} else if (isColor) {
			 return ClientSettings.colorColor.asInt();
		} else if (isLabel) {
			return ClientSettings.labelColor.asInt();
		} else if (toggled) {
			return ClientSettings.textColor.asInt();
		} else {
			return ClientSettings.textColorOff.asInt();
		}
	}
	
	//Get the top parent of this node
	public GuiNode getTopParent() {
		ArrayList<GuiNode> parents = getAllParents();
		return parents.get(parents.size() - 1);
	}
	
	//Gets all parents from this node. First in list is this objects parent
	public ArrayList<GuiNode> getAllParents() {
		ArrayList<GuiNode> parents = new ArrayList<>();
		
		GuiNode parent = this.parent;
		while(true) {
			if (parent != null) {
				parents.add(parent);
				
				if (parent.parent != null) {
					parent = parent.parent;
					continue;
				}
			}
			
			break;
		}
		
		return parents;
	}
	
	public void notifyClickListeners() {
		for (ClickListener listener : clickListeners) {
			listener.clicked();
		}
	}
	
	public void notifyKeyListeners() {
		for (KeyListener listener : keyListeners) {
			listener.pressed();
		}	
	}
	
	public static class ClickListener {
		public void clicked() {
			
		}
	}
	
	public static class KeyListener {
		public void pressed() {
			
		}
	}
}
