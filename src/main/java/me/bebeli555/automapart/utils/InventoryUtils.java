package me.bebeli555.automapart.utils;

import me.bebeli555.automapart.Mod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils extends Mod {
    /**
     * Gets the itemstack in the given slot.
     */
    public static ItemStack getStack(int slot) {
        return mc.player.playerScreenHandler.getStacks().get(slot);
    }

    /**
     * Counts how many items there are in the inventory (summing stack sizes).
     */
    public static int getAmountOfItem(Item item) {
        int count = 0;
        for (ItemStack stack : mc.player.playerScreenHandler.getStacks()) {
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * Returns the number of free slots in the inventory.
     */
    public static int getFreeSpace() {
        int count = -10;
        for (ItemStack stack : mc.player.playerScreenHandler.getStacks()) {
            if (stack.isEmpty()) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * Returns the hotbar slot index (0-8) where the given item is found.
     */
    public static int getHotbarSlot(Item item) {
        for (int i = 36; i < 44; i++) {
            if (mc.player.playerScreenHandler.getSlot(i).getStack().getItem() == item) {
                return i - 36;
            }
        }
        return -1;
    }

    /**
     * Switches to the given item by either selecting it from the hotbar or quick-moving it into the hotbar.
     */
    public static void switchToItem(Item item) {
        int hotbarSlot = getHotbarSlot(item);
        if (hotbarSlot != -1) {
            mc.player.getInventory().selectedSlot = hotbarSlot;
            return;
        }

        for (Slot slot : mc.player.currentScreenHandler.slots) {
            if (slot.getStack().getItem() == item) {
                quickMove(slot.id);
                sleep(100);
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                hotbarSlot = getHotbarSlot(item);
                if (hotbarSlot != -1) {
                    mc.player.getInventory().selectedSlot = hotbarSlot;
                }
                return;
            }
        }
    }

    /**
     * Gets a list of all the item stacks in the inventory along with their slot indices.
     */
    public static List<StackAndSlot> getStacksWithSlots() {
        List<StackAndSlot> list = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            list.add(new StackAndSlot(getStack(i), i));
        }
        return list;
    }

    /**
     * Gets all the item stacks in the inventory.
     */
    public static List<ItemStack> getStacks() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            list.add(getStack(i));
        }
        return list;
    }

    /**
     * Performs a quick move (shift-click) on the specified slot.
     */
    public static void quickMove(int id) {
        if (id != -1) {
            try {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, 0, SlotActionType.QUICK_MOVE, mc.player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Throws away the item in the given slot using the THROW action.
     */
    public static void throwAway(int id) {
        if (id != -1) {
            try {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, 1, SlotActionType.THROW, mc.player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Drops the item in the specified slot without switching the held item.
     * This method is similar to throwAway and sends a THROW action on the slot.
     */
    public static void dropItem(int id) {
        if (id != -1) {
            try {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, id, 1, SlotActionType.THROW, mc.player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public record StackAndSlot(ItemStack itemStack, int slot) {}
}
