package me.bebeli555.automapart.command.commands;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;
import me.bebeli555.automapart.events.player.PlayerDisplayNameEvent;
import me.bebeli555.automapart.utils.ClientUtils;

import java.util.ArrayList;
import java.util.List;

public class ChangeNameCommand extends Command {
    public static List<String[]> names = new ArrayList<>();
    public boolean subscribed;

    public ChangeNameCommand() {
        super("changename", "Changes the rendered name of a player", new CommandParameter(0, "oldname", "newname"), new CommandParameter(0, "clear"));
    }

    @Override
    public void onCommand(String[] parameter) {
        if (parameter[0].equals("clear")) {
            ClientUtils.sendMessage("Cleared all renames");
            names.clear();
            return;
        }

        if (!subscribed) {
            Mod.EVENT_BUS.register(this);
        }

        subscribed = true;

        names.add(new String[]{parameter[0], parameter[1]});
        ClientUtils.sendMessage("Renamed " + parameter[0] + " to " + parameter[1]);
    }

    @Subscribe
    private void playerDisplayNameEvent(PlayerDisplayNameEvent event) {
        event.name = getChangedName(event.player.getName().getString());
    }

    public static String getChangedName(String oldName) {
        for (String[] name : names) {
            if (name[0].equals(oldName)) {
                return name[1].replace("&", "\u00a7");
            }
        }

        return oldName;
    }
}
