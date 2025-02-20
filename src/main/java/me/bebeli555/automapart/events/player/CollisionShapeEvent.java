package me.bebeli555.automapart.events.player;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public class CollisionShapeEvent extends Cancellable {
    public BlockState state;
    public BlockPos pos;
    public VoxelShape shape;

    public CollisionShapeEvent(BlockState state, BlockPos pos, VoxelShape shape) {
        this.state = state;
        this.pos = pos;
        this.shape = shape;
    }
}
