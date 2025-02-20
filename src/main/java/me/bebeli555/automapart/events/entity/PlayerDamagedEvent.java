package me.bebeli555.automapart.events.entity;

import net.minecraft.entity.damage.DamageSource;

public class PlayerDamagedEvent {
    public DamageSource source;
    public float amount;

    public PlayerDamagedEvent(DamageSource source, float amount) {
        this.source = source;
        this.amount = amount;
    }
}
