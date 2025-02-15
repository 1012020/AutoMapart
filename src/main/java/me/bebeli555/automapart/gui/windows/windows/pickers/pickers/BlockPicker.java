package me.bebeli555.automapart.gui.windows.windows.pickers.pickers;

import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockPicker extends PickerWindow {
    public static BlockPicker INSTANCE;

    public Map<BlockAndSetting, Boolean> validHash = new HashMap<>();
    public static List<SettingValueChangedListener> listeners = new ArrayList<>();

    public BlockPicker() {
        super("BlockPicker");
        INSTANCE = this;

        for (Block block : Registries.BLOCK) {
            Item item = block.asItem();
            add(new PickerItem(block.getName().getString(), item == Items.AIR ? block : item, "", block));
        }

        this.addItemSelectedListener(new Setting.ValueChangedListener(){
            public void valueChanged() {
                validHash.clear();

                for (SettingValueChangedListener listener : listeners) {
                    if (listener.setting == currentSetting) {
                        listener.listener.valueChanged();
                    }
                }
            }
        });
    }

    public static boolean isValid(Block block, Setting setting) {
        Boolean value = INSTANCE.validHash.get(new BlockAndSetting(block, setting));
        if (value != null) {
            return value;
        }

        List<PickerItem> selected = INSTANCE.getSelectedFromSetting(setting);
        boolean valid = selected.stream().anyMatch(item -> item.name.equalsIgnoreCase(block.getName().getString()));
        INSTANCE.validHash.put(new BlockAndSetting(block, setting), valid);

        return valid;
    }

    public record BlockAndSetting(Block block, Setting setting) {
        @Override
        public boolean equals(Object object) {
            BlockAndSetting second = (BlockAndSetting)object;
            return second.block == this.block && second.setting == this.setting;
        }
    }

    public void addValueChangedListener(SettingValueChangedListener listener) {
        listeners.add(listener);
    }

    public record SettingValueChangedListener(Setting setting, Setting.ValueChangedListener listener) {}
}
