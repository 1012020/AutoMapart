package me.bebeli555.automapart.settings;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.windows.other.TextArrayWindow;
import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.utils.Utils;

public class Setting {
	public Mode mode;
	public String name;
	public String[] description;
	private Object value;
	public Object defaultValue;
	public double minValue, maxValue, step;
	public boolean saveSetting = true;
	public BooleanSupplier isVisible = () -> true;
	public Setting parent;
	public String id;
	public String modeName = "";
	public Class<?> pickerClass;
	public ArrayList<String> modes = new ArrayList<>();
	public ArrayList<ArrayList<String>> modeDescriptions = new ArrayList<>();
	public ArrayList<ValueChangedListener> listeners = new ArrayList<>();
	
	public static ArrayList<Setting> all = new ArrayList<>();
	public static HashMap<String, Setting> settingHash = new HashMap<>();

	public Setting() {}

	public record PickerName(String name, Class<?> pickerClass) {}
	public Setting(Mode mode, PickerName pickerName, Object defaultValue, String... description) {
		this(mode, pickerName.name, defaultValue, description);
		this.pickerClass = pickerName.pickerClass;
	}

	public Setting(Mode mode, String name, Object defaultValue, BooleanSupplier isVisible, String... description) {
		if (defaultValue instanceof SettingValue settingValue) {
			this.defaultValue = settingValue.value;
			this.value = settingValue.value;
			this.minValue = settingValue.minValue;
			this.maxValue = settingValue.maxValue;
			this.step = settingValue.step;
		} else {
			this.defaultValue = defaultValue;
			this.value = defaultValue;
		}

		this.mode = mode;
		this.name = name;
		this.description = description;
		setId();
		all.add(this);
		settingHash = all.stream().collect(Collectors.toMap(s -> s.id.toLowerCase(), Function.identity(), (a, b) -> b, HashMap::new));

		this.isVisible = isVisible;
	}

	public Setting(Mode mode, String name, Object defaultValue, String... description) {
		this(mode, name, defaultValue, () -> true, description);
	}

	public Setting(Setting parent, Mode mode, String name, Object defaultValue, String... description) {
		this(mode, name, defaultValue, description);
		this.parent = parent;
		setId();
	}

	public Setting(Setting parent, Mode mode, String name, Object defaultValue, BooleanSupplier isVisible, String... description) {
		this(parent, mode, name, defaultValue, description);
		this.isVisible = isVisible;
	}

	public Setting(Setting parent, Mode mode, String name, Object defaultValue, boolean saveSetting, String... description) {
		this(parent, mode, name, defaultValue, description);
		this.saveSetting = saveSetting;
	}

	public Setting(Setting parent, String modeName, Mode mode, String name, Object defaultValue, String... description) {
		this(mode, name, defaultValue, description);
		this.parent = parent;
		this.modeName = modeName;
		setId();
	}

	public Setting(Setting parent, ModeListType modeName, Mode mode, String name, Object defaultValue, String... description) {
		this(parent, modeName.name, mode, name, defaultValue, description);
	}

	/**
	 * Used for creating a mode thing
	 * @param modes first string is the name and the others will be description.
	 */
	public Setting(Setting parent, String name, String defaultValue, String[]... modes) {
		for (String[] mode : modes) {
			this.modes.add(mode[0]);

			ArrayList<String> descriptions = new ArrayList<>(Arrays.asList(mode).subList(1, mode.length));
			this.modeDescriptions.add(descriptions);
		} 
		
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.parent = parent;
		this.name = name;
		setId();
		all.add(this);
		settingHash = all.stream().collect(Collectors.toMap(s -> s.id.toLowerCase(), Function.identity(), (a, b) -> b, HashMap::new));
	}

	public Setting(String name, String defaultValue, String[]... modes) {
		this(null, name, defaultValue, modes);
	}

	public Setting(Setting parent, String name, String defaultValue, Class<?> listType) {
		this(parent, name, defaultValue, Objects.requireNonNullElse(getModesFromClass(listType), new String[][]{}));
	}

	public Setting(Setting parent, String name, String defaultValue, List<ModeListType> modes) {
		this(parent, name, defaultValue, getModesFromList(modes));
	}

	private static String[][] getModesFromList(List<ModeListType> list) {
		List<String[]> modes = new ArrayList<>();
		for (ModeListType mode : list) {
			modes.add(new String[]{mode.name, mode.description});
		}

		return modes.toArray(new String[0][]);
	}

	private static String[][] getModesFromClass(Class<?> clazz) {
		try {
			List<String[]> modes = new ArrayList<>();
			for (Field field : clazz.getFields()) {
				ModeListType mode = (ModeListType)field.get(null);
				modes.add(new String[]{mode.name, mode.description});
			}

			return modes.toArray(new String[0][]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean hasSetMinAndMaxValues() {
		return maxValue != 0 || minValue != 0;
	}

	public void setValue(Object value) {
		if (!this.value.equals(value)) {
			Object oldValue = this.value;
			this.value = value;

			if (!Mod.modInitializationDone) {
				return;
			}

			for (ValueChangedListener listener : listeners) {
				if (!listener.onlyIfModuleIsOn || listener.module == null || listener.module.isOn()) {
					listener.valueChanged();
					
					if (listener.cancelled) {
						this.value = oldValue;
						listener.cancelled = false;
						updateGuiNode();
						return;
					}
				}
			}
		}
	}
	
	public boolean bool() {
		return (Boolean)value;
	}
	
	public int asInt() {
		if (string().isEmpty()) {
			return -1;
		}
		
		try {
			return (int)value;
		} catch (ClassCastException e) {
			try {
				return Integer.parseUnsignedInt(((String)value).replace("0x", ""), 16);
			} catch (Exception e2) {
				try {
					return Integer.parseUnsignedInt(((String)defaultValue).replace("0x", ""), 16);
				} catch (Exception e3) {
					return -1;
				}
			}
		}
	}

	public double asDouble() {
		if (string().isEmpty()) {
			return -1;
		}
		
		try {
			return (double)value;
		} catch (Exception e) {
			try {
				return (int)value;
			} catch (Exception e2) {
				try {
					return (double)defaultValue;
				} catch (Exception e3) {
					return -1;
				}
			}
		}
	}

	public List<String> asPickerList() {
		List<String> list = new ArrayList<>();
		for (PickerWindow.PickerItem item : getPickerWindow().selected) {
			list.add(item.name);
		}

		return list;
	}

	public List<String> asTextArray() {
		List<String> list = new ArrayList<>();
		for (String s : string().replace(TextArrayWindow.prefix, ":").split("(?<!\\\\), ")) {
			list.add(s.replace("\\,", ","));
		}

		return list;
	}

	public PickerWindow getPickerWindow() {
		for (TitledWindow window : TitledWindow.list) {
			if (this.pickerClass == null) {
				if (window instanceof PickerWindow && window.title.equals(this.name)) {
					return (PickerWindow)window;
				}
			} else if (window instanceof PickerWindow && this.pickerClass.getName().equals(window.getClass().getName())) {
				return (PickerWindow)window;
			}
		}

		return null;
	}

	public float asFloat() {
		return (float) asDouble();
	}

	public String string() {
		return String.valueOf(value);
	}

	public void updateGuiNode() {
		if (mode == Mode.BOOLEAN) {
			Settings.getGuiNodeFromId(id).toggled = bool();
		} else {
			Settings.getGuiNodeFromId(id).stringValue = string();
		}
	}

	public void setId() {
		id = "";
		
		if (this.parent != null) {
			List<Setting> parents = getParents();

			for (int i = parents.size(); i-- > 0;) {
				id += parents.get(i).name;
			}
			
			id += this.name;
		} else {
			id = this.name;
		}
		
		id += modeName;
	}
	
	/**
	 * Get all parents for this setting
	 */
	public List<Setting> getParents() {
		List<Setting> list = new ArrayList<>();
		
		Setting parent = this.parent;
		while(true) {
			if (parent != null) {
				list.add(parent);
				
				if (parent.parent != null) {
					if (parent.parent.equals(parent)) {
						break;
					}

					parent = parent.parent;
					continue;
				}
			}
			
			break;
		}
		
		return list;
	}

	/**
	 * Get all this settings children
	 */
	private final HashMap<String, List<Setting>> childrenMap = new HashMap<>();
	public List<Setting> getChildren(String add) {
		List<Setting> mapList = childrenMap.get(add);
		if (mapList != null) {
			return mapList;
		}

		List<Setting> list = new ArrayList<>();
		for (Setting setting : all) {
			if ((add + setting.id).startsWith(this.id) && !setting.equals(this)) {
				list.add(setting);
			}
		}

		childrenMap.put(add, list);
		return list;
	}

	public void addValueChangedListener(ValueChangedListener listener) {
		listeners.add(listener);
	}

	public void addValueChangedListener(Runnable runnable, Mod mod) {
		listeners.add(new ValueChangedListener(mod, mod != null){
			public void valueChanged() {
				runnable.run();
			}
		});
	}

	public void addValueChangedListenerWithDelay(Runnable runnable, Mod mod) {
		final long[] lastChange = {0};
		listeners.add(new ValueChangedListener(mod, mod != null){
			public void valueChanged() {
				lastChange[0] = System.currentTimeMillis();
				new Thread(() -> {
					Utils.sleep(500);
					if (Math.abs(System.currentTimeMillis() - lastChange[0]) >= 450) {
						Utils.mc.execute(runnable);
					}
				}).start();
			}
		});
	}

	public static Setting getSettingWithId(String id) {
		return settingHash.get(id.toLowerCase());
	}
	
	public static class ValueChangedListener {
		public Mod module;
		public boolean onlyIfModuleIsOn = true;
		public boolean cancelled;

		public ValueChangedListener() {}
		public ValueChangedListener(Mod module, boolean onlyIfModuleIsOn) {
			this.module = module;
			this.onlyIfModuleIsOn = onlyIfModuleIsOn;
		}
		
		/**
		 * Gets called when the value changes.
		 */
		public void valueChanged() {}
		public void valueChanged(Object value) {}

		/**
		 * Cancels the change and puts the old value back
		 */
		public void cancel() {
			cancelled = true;
		}
	}

	public record ModeListType(String name, String description) {
		@Override
		public String toString() {
			return name;
		}
	}
}
