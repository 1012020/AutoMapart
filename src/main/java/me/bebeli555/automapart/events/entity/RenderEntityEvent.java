package me.bebeli555.automapart.events.entity;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.entity.LivingEntity;

public class RenderEntityEvent extends Cancellable {
    public LivingEntity entity;

    public RenderEntityEvent(LivingEntity entity) {
        this.entity = entity;
    }
}
