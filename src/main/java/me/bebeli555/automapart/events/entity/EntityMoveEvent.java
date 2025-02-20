package me.bebeli555.automapart.events.entity;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

public class EntityMoveEvent extends Cancellable {
    public Entity entity;
    public MovementType movementType;
    public Vec3d movement;

    public EntityMoveEvent(Entity entity, MovementType movementType, Vec3d movement) {
        this.entity = entity;
        this.movementType = movementType;
        this.movement = movement;
    }
}
