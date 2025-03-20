package me.bebeli555.automapart.gui.windows.windows.pickers.pickers;

import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.settings.Setting;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemPicker extends PickerWindow {
    public static ItemPicker INSTANCE;

    private static Map<ItemAndSetting, Boolean> validHash = new HashMap<>();

    public ItemPicker() {
        super("ItemPicker");
        INSTANCE = this;

        for (Item item : Registries.ITEM) {
            add(new PickerItem(item.getName().getString(), item, "", item));
        }

        this.addItemSelectedListener(new Setting.ValueChangedListener(){
            public void valueChanged() {
                validHash.clear();
            }
        });
    }

    public static boolean isValid(Item item, Setting setting) {
        Boolean value = validHash.get(new ItemAndSetting(item, setting));
        if (value != null) {
            return value;
        }

        List<PickerItem> selected = INSTANCE.getSelectedFromSetting(setting);
        boolean valid = selected.stream().anyMatch(i -> i.name.equalsIgnoreCase(item.getName().getString()));
        validHash.put(new ItemAndSetting(item, setting), valid);

        return valid;
    }

    public record ItemAndSetting(Item item, Setting setting) {
        public boolean equals(Object object) {
            ItemAndSetting second = (ItemAndSetting)object;
            return second.item == this.item && second.setting == this.setting;
        }
    }
}
