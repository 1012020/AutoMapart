package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import net.minecraft.text.Text;

public class DisconnectCommand extends Command {
    public DisconnectCommand() {
        super("disconnect", "Disconnects you from the server");
    }

    @Override
    public void onCommand(String[] parameter) {
        mc.player.networkHandler.getConnection().disconnect(Text.literal("Sierra: disconnected!"));
    }
}
