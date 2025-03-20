package me.bebeli555.automapart.events.entity;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.entity.Entity;

public class IsEntityGlowingEvent extends Cancellable {
    public Entity entity;
    public boolean glowing;

    public IsEntityGlowingEvent(Entity entity) {
        this.entity = entity;
    }
}
