package me.bebeli555.automapart.utils.objects;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.events.block.BlockActivateEvent;
import me.bebeli555.automapart.events.game.SetScreenEvent;
import me.bebeli555.automapart.utils.Utils;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class EnderchestMemory {
    public static final DefaultedList<ItemStack> ITEMS = DefaultedList.ofSize(27, ItemStack.EMPTY);

    private boolean check;

    @Subscribe
    public void blockActivate(BlockActivateEvent event) {
        check = event.state.getBlock() instanceof EnderChestBlock;
    }

    @Subscribe
    public void setScreen(SetScreenEvent event) {
        if (Utils.mc.currentScreen instanceof GenericContainerScreen container && check) {
            check = false;

            Inventory inv = container.getScreenHandler().getInventory();
            for (int i = 0; i < 27; i++) {
                ITEMS.set(i, inv.getStack(i));
            }
        }
    }
}
