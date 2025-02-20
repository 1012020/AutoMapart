package me.bebeli555.automapart.events.entity;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.entity.Entity;

public class EntityRemovedEvent extends Cancellable {
    public Entity entity;
    public Entity.RemovalReason removalReason;

    public EntityRemovedEvent(Entity entity, Entity.RemovalReason removalReason) {
        this.entity = entity;
        this.removalReason = removalReason;
    }
}
