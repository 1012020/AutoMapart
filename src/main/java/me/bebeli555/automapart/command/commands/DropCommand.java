package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;

public class DropCommand extends Command {
    public DropCommand() {
        super("drop", "Drop items from inventory",
                new CommandParameter.CommandParameterGroup(
                        new CommandParameter(0, "slotid/itemname", "quantity")
                )
        );
    }

    @Override
    public void onCommand(String[] parameter) {
        //TODO
    }
}
