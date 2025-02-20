package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.utils.InventoryUtils;
import me.bebeli555.automapart.utils.PlayerUtils;
import me.bebeli555.automapart.utils.RotationUtils;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class GuiCommand extends Command {
    public GuiCommand() {
        super("gui", "Opens the GUI");
    }

    @Override
    public void onCommand(String[] parameter) {
        Gui.openGui = true;
        Mod.EVENT_BUS.register(Gui.registerGui);
    }
}
