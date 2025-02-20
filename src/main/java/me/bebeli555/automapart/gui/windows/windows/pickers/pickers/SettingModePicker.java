package me.bebeli555.automapart.gui.windows.windows.pickers.pickers;

import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.settings.Setting;
import net.minecraft.item.Items;

public class SettingModePicker extends PickerWindow {
    public static SettingModePicker INSTANCE;

    public SettingModePicker() {
        super("ModePicker");
        this.allowOnlyOneSelected = true;
        INSTANCE = this;
    }

    public void setMode(Setting setting) {
        list.clear();
        selected.clear();

        for (String mode : setting.modes) {
            add(new PickerItem(mode, Items.ENCHANTED_BOOK));
        }
    }
}
