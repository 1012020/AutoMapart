package me.bebeli555.automapart.events.player;

import net.minecraft.entity.player.PlayerEntity;

public class PlayerDisplayNameEvent {
    public String name;
    public PlayerEntity player;

    public PlayerDisplayNameEvent(PlayerEntity player) {
        this.player = player;
    }
}
