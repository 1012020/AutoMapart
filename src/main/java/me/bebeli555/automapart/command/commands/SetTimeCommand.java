package me.bebeli555.automapart.command.commands;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;
import me.bebeli555.automapart.events.render.SetWorldTimeEvent;
import me.bebeli555.automapart.utils.ClientUtils;

public class SetTimeCommand extends Command {
    public SetTimeCommand() {
        super("settime", "Sets the clientsided time",
                new CommandParameter(0, "number/disable")
        );
    }

    @Override
    public void onCommand(String[] parameter) {
        try {
            if (parameter[0].equals("disable")) {
                Mod.EVENT_BUS.unregister(this);
                ClientUtils.sendMessage("Disabled time changer");
                return;
            }
        } catch (Exception ignored) {}

        int time = Integer.parseInt(parameter[0]) + 900000;
        mc.world.getLevelProperties().timeOfDay = time;
        Mod.EVENT_BUS.register(this);

        ClientUtils.sendMessage("Set client time to " + time);
    }

    @Subscribe
    public void setWorldTimeEvent(SetWorldTimeEvent event) {
        event.cancel();
    }
}
