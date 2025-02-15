package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;

public class ChatCommand extends Command {
    public ChatCommand() {
        super("chat", "Sends a chat message or command (for macros lol)", new CommandParameter(0, "message"));
    }

    @Override
    public void onCommand(String[] parameter) {
        String value = getStringValue(parameter);
        if (value.startsWith("/")) {
            mc.getNetworkHandler().sendCommand(value.substring(1));
        } else {
            mc.getNetworkHandler().sendChatMessage(value);
        }
    }
}
