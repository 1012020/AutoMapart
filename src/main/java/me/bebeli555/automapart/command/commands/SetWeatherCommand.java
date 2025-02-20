package me.bebeli555.automapart.command.commands;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;
import me.bebeli555.automapart.events.game.ClientTickEvent;
import me.bebeli555.automapart.utils.ClientUtils;

public class SetWeatherCommand extends Command {
    public String weather;

    public SetWeatherCommand() {
        super("setweather", "Sets the clientsided weather",
                new CommandParameter(0, "clear/rain/disable")
        );

        Mod.EVENT_BUS.register(this);
    }

    @Override
    public void onCommand(String[] parameter) {
        String weather = parameter[0];
        this.weather = weather;

        if (weather.equals("disable")) {
            ClientUtils.sendMessage("Disabled weather changer");
        } else {
            ClientUtils.sendMessage("Set clientsided weather to " + weather);
        }
    }

    @Subscribe
    public void tickEvent(ClientTickEvent e) {
        if (weather != null && !weather.equals("disable") && mc.player != null) {
            if (weather.equals("clear")) {
                mc.player.getWorld().getLevelProperties().setRaining(false);
                mc.player.getWorld().setRainGradient(0.0f);
            } else if (weather.equals("rain")) {
                mc.player.getWorld().getLevelProperties().setRaining(true);
                mc.player.getWorld().setRainGradient(1.0f);
            }
        }
    }
}
