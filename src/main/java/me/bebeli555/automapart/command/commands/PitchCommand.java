package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;

public class PitchCommand extends Command {
    public PitchCommand() {
        super("pitch", "Set the player pitch", new CommandParameter(0, "pitch"));
    }

    @Override
    public void onCommand(String[] parameter) {
        mc.player.setPitch(Float.parseFloat(parameter[0]));
    }
}
