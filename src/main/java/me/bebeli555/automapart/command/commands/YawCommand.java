package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;

public class YawCommand extends Command {
    public YawCommand() {
        super("yaw", "Set the player yaw", new CommandParameter(0, "yaw"));
    }

    @Override
    public void onCommand(String[] parameter) {
        mc.player.setYaw(Float.parseFloat(parameter[0]));
    }
}
