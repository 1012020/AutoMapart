package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.utils.ClientUtils;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand extends Command {
    public static HelpCommand INSTANCE;

    public HelpCommand() {
        super("help", "Get a list of available commands");
        INSTANCE = this;
    }

    @Override
    public void onCommand(String[] parameter) {
        List<String[]> list = new ArrayList<>();
        for (Command command : Command.list) {
            String[] s = {
                    Formatting.byName(ClientSettings.clientMessageColor.string()) + command.name + ", " + (command.newLine ? "\n" : ""),
                    command.description + "\n" + command.getUsageHint(),
                    command.getUsageHint()
            };

            list.add(s);
        }

        list.set(list.size() - 1, new String[]{list.get(list.size() - 1)[0].replace(", ", ""), list.get(list.size() - 1)[1], list.get(list.size() - 1)[2]});
        ClientUtils.sendMessage(Formatting.DARK_AQUA + "Commands (" + Command.list.size() + "): (Hover over them)");
        ClientUtils.sendMessage(list);
    }
}
