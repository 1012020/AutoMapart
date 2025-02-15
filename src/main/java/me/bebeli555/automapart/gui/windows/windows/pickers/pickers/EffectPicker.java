package me.bebeli555.automapart.gui.windows.windows.pickers.pickers;

import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;

public class EffectPicker extends PickerWindow {
    public EffectPicker() {
        super("EffectPicker");

        for (StatusEffect effect : Registries.STATUS_EFFECT) {
            add(new PickerItem(effect.getName().getString(), effect));
        }
    }
}
