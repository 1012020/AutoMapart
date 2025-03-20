package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;
import me.bebeli555.automapart.hud.components.InfoClusterComponent;
import me.bebeli555.automapart.utils.ClientUtils;
import net.minecraft.client.network.PlayerListEntry;

import java.util.HashMap;
import java.util.stream.Collectors;

public class SeenCommand extends Command {
    public HashMap<String, Long> hash = new HashMap<>();

    public SeenCommand() {
        super("seen", "Shows how long ago a player was online",
                new CommandParameter(0, "username", () -> {
                    active.completions = mc.getNetworkHandler().getPlayerList().stream().map(p -> p.getProfile().getName()).collect(Collectors.toList());
                })
        );

        new Thread(() -> {
            while(true) {
                try {
                    update();
                } catch (Exception ignored) {}

                sleep(5000);
            }
        }).start();
    }

    @Override
    public void onCommand(String[] parameter) {
        Long value = hash.get((String)parameter[0]);
        if (value == null) {
            ClientUtils.sendMessage("No data for " + parameter[0]);
        } else {
            if (Math.abs(System.currentTimeMillis() - value) <= 8000) {
                ClientUtils.sendMessage(parameter[0] + " is online!");
            } else {
                ClientUtils.sendMessage(parameter[0] + " was seen" + InfoClusterComponent.getTimeAgoString(value, true) + " ago");
            }
        }
    }

    public void update() {
        if (mc.player != null) {
            for (PlayerListEntry player : mc.player.networkHandler.getListedPlayerListEntries()) {
                hash.put(player.getProfile().getName(), System.currentTimeMillis());
            }
        }
    }
}
