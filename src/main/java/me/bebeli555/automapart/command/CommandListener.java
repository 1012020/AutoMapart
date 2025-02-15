package me.bebeli555.automapart.command;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.ClientChatEvent;
import me.bebeli555.automapart.mods.ClientSettings;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;

public class CommandListener extends Mod {
    @Subscribe
    public void onChat(ClientChatEvent e) {
        String message = e.getMessage();
        if (message.startsWith(ClientSettings.prefix.string())) {
            e.cancel();
            List<String> sentMessages = mc.inGameHud.getChatHud().getMessageHistory();
            if (sentMessages.size() == 0 || !sentMessages.get(sentMessages.size() - 1).equals(message)) {
                sentMessages.add(message);
            }

            String[] split = message.substring(ClientSettings.prefix.string().length()).split(" ");
            Command command = Command.getCommand(split[0]);
            if (command == null) {
                sendMessage(Command.getCommandNotFoundHint(), Formatting.RED);
                return;
            }

            String[] parameter = Arrays.copyOfRange(split, 1, split.length);
            try {
                command.onCommand(parameter);
            } catch (Exception ignored) {
                sendMessage("Invalid usage! " + command.getUsageHint(), Formatting.RED);
            }
        }
    }
}
