package me.bebeli555.automapart.settings;

import me.bebeli555.automapart.Mod;
import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.events.game.KeyInputEvent;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.utils.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class Keybind extends Mod {
	public static List<KeybindValue> list = new ArrayList<>();
	private static List<KeybindReleasedListener> listeners = new ArrayList<>();

	@Subscribe
	private void onKey(KeyInputEvent e) {
 		if (e.getAction() == 0) {
			for (KeybindValue keybind : list) {
				if (keybind.key == e.getKey()) {
					keybind.clicked = false;

					//Call release listeners
					for (KeybindReleasedListener listener : listeners) {
						if (keybind.id.startsWith(listener.mod.name)) {
							listener.released();
						}
					}
				}
			}
			
			return;
		}

		if (mc.currentScreen != null) {
			return;
		}

		for (KeybindValue keybind : list) {
			if (keybind.key == e.getKey()) {
				if (keybind.clicked) {
					continue;
				}
				
				if (keybind.id.equals("Keybind")) {
					if (Gui.isOpen) {
						continue;
					}

					Gui.openGui = true;
					Mod.EVENT_BUS.register(Gui.registerGui);
				} else {
					GuiNode node = Settings.getGuiNodeFromId(keybind.id.replace("Keybind", ""));
					node.click();
				}

				keybind.clicked = true;
			}
		}
	}
	
	//Sets the hashmap of keybinds so checking them will take less resources than looping all the nodes
	public static void setKeybinds() {
		list.clear();
		
		for (GuiNode node : GuiNode.all) {
			if (node.isKeybind) {
				if (node.stringValue != null && !node.stringValue.isEmpty()) {
					try {
						int key = Integer.parseInt(node.stringValue);
						list.add(new KeybindValue(key, node.id));
						node.renderValue = Keyboard.getKeyName(key);
					} catch (NumberFormatException ignored) {}
				}
			}
		}
	}

	public static void addReleasedListener(Runnable runnable, Mod mod) {
		listeners.add(new KeybindReleasedListener(mod){
			public void released() {
				runnable.run();
			}
		});
	}

	public static class KeybindValue {
		public int key;
		public String id;
		public boolean clicked;
		
		public KeybindValue(int key, String id) {
			this.key = key;
			this.id = id;
		}
	}

	public static class KeybindReleasedListener {
		public Mod mod;

		public KeybindReleasedListener(Mod mod) {
			this.mod = mod;
		}

		public void released() {}
	}
}
