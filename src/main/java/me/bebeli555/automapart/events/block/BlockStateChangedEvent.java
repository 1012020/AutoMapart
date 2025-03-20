package me.bebeli555.automapart.events.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockStateChangedEvent {
    public BlockPos pos;
    public BlockState state;

    public BlockStateChangedEvent(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }
}
