package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;
import me.bebeli555.automapart.hud.components.StopWatchComponent;

public class StopWatchCommand extends Command {
    public StopWatchCommand() {
        super("stopwatch", "Toggle stopwatch on or off or reset it",
                new CommandParameter(0, "toggle/reset")
        );
    }

    @Override
    public void onCommand(String[] parameter) {
        if (parameter == null || parameter.length == 0 || parameter[0].equals("toggle")) {
            StopWatchComponent.INSTANCE.running = !StopWatchComponent.INSTANCE.running;
        } else if (parameter[0].equals("reset")) {
            StopWatchComponent.INSTANCE.reset = true;
        }
    }
}
