package me.bebeli555.automapart.gui.windows.windows.pickers.pickers;

import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;

public class PotionPicker extends PickerWindow {
    public PotionPicker() {
        super("PotionPicker");

        for (Potion potion : Registries.POTION) {
            ItemStack item = PotionUtil.setPotion(new ItemStack(Items.POTION), potion);
            add(new PickerItem(item.getName().getString(), item));
        }
    }
}
