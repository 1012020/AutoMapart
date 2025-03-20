package me.bebeli555.automapart.events.player;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

public class PlayerMoveEvent extends Cancellable {
    public MovementType type;
    public Vec3d movement;

    public PlayerMoveEvent(MovementType type, Vec3d movement) {
        this.type = type;
        this.movement = movement;
    }
}
