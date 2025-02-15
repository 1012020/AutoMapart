package me.bebeli555.automapart.gui.windows.windows.pickers.pickers;

import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.settings.Setting;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.List;

public class EnchantmentPicker extends PickerWindow {
    public static EnchantmentPicker INSTANCE;

    public EnchantmentPicker() {
        super("EnchantmentPicker");
        INSTANCE = this;

        for (Enchantment enchantment : Registries.ENCHANTMENT) {
            add(new PickerItem(enchantment.getName(1).getString().replace(" I", ""), getItemForEnchantment(enchantment)));
        }
    }

    public Object getItemForEnchantment(Enchantment enchantment) {
        Item item;
        if (enchantment.target == EnchantmentTarget.ARMOR) {
            return StatusEffects.RESISTANCE;
        } else if (enchantment.target == EnchantmentTarget.WEAPON) {
            return StatusEffects.STRENGTH;
        } else if (enchantment.target == EnchantmentTarget.BOW) {
            item = Items.BOW;
        } else if (enchantment.target == EnchantmentTarget.FISHING_ROD) {
            item = Items.FISHING_ROD;
        } else if (enchantment.target == EnchantmentTarget.TRIDENT) {
            item = Items.TRIDENT;
        } else if (enchantment.target == EnchantmentTarget.CROSSBOW) {
            item = Items.CROSSBOW;
        } else if (enchantment.target == EnchantmentTarget.DIGGER) {
            return StatusEffects.HASTE;
        } else if (enchantment.target == EnchantmentTarget.ARMOR_HEAD) {
            item = Items.DIAMOND_HELMET;
        } else if (enchantment.target == EnchantmentTarget.ARMOR_FEET) {
            item = Items.DIAMOND_BOOTS;
        } else if (enchantment.target == EnchantmentTarget.ARMOR_CHEST) {
            item = Items.DIAMOND_CHESTPLATE;
        } else {
            item = Items.ENCHANTED_BOOK;
        }

        ItemStack stack = new ItemStack(item);
        stack.addEnchantment(enchantment, 1);

        return stack;
    }

    public boolean isValid(Enchantment enchantment, Setting setting) {
        List<PickerItem> selected = getSelectedFromSetting(setting);
        for (PickerItem item : selected) {
            if (item.name.equals(enchantment.getName(1).getString().replace(" I", ""))) {
                return true;
            }
        }

        return false;
    }
}
