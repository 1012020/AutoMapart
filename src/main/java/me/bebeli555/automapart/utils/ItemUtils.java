package me.bebeli555.automapart.utils;

import me.bebeli555.automapart.Mod;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Formatting;

public class ItemUtils extends Mod {
	/**
	 * Get current durability for the itemstack in percentage points
	 */
	public static int getPercentageDurability(ItemStack itemStack) {
		return (int)(((double)getDurability(itemStack) / (double)itemStack.getMaxDamage()) * 100);
	}
	
	/**
	 * Checks if the itemStack has durability like can be broken
	 */
	public static boolean hasDurability(ItemStack itemStack) {
		return itemStack.getMaxDamage() != 0;
	}
	
	
	/**
	 * Gets the durability color like green, yellow, red
	 */
	public static Formatting getDurabilityColor(ItemStack itemStack) {
		Formatting color = Formatting.GREEN;
		int durability = ItemUtils.getPercentageDurability(itemStack);
		
		if (durability < 20) {
			color = Formatting.RED;
		} else if (durability < 60) {
			color = Formatting.GOLD;
		}
		
		return color;
	}
	
	/**
	 * Get durability for the itemStack
	 */
	public static int getDurability(ItemStack itemStack) {
		return itemStack.getMaxDamage() - itemStack.getDamage();
	}

	/**
	 * Checks if this itemstack has other items stored inside it
	 */
	public static boolean hasItems(ItemStack itemStack) {
		NbtCompound compoundTag = itemStack.getSubNbt("BlockEntityTag");
		return compoundTag != null && compoundTag.contains("Items", 9);
	}
}
