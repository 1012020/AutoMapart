package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.utils.ClientUtils;

public class NoPauseCommand extends Command {
    public NoPauseCommand() {
        super("nopause", "toggles pause if lost focus setting");
        this.newLine = true;
    }

    @Override
    public void onCommand(String[] parameter) {
        mc.options.pauseOnLostFocus = !mc.options.pauseOnLostFocus;
        ClientUtils.sendMessage("Set to: " + mc.options.pauseOnLostFocus);
    }
}
