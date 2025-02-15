package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;

public class ClearCommand extends Command {
    public ClearCommand() {
        super("clear", "Clears the clientsided chatbox");
    }

    @Override
    public void onCommand(String[] parameter) {
        mc.inGameHud.getChatHud().clear(true);
    }
}
