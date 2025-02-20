package me.bebeli555.automapart.events.block;

import net.minecraft.block.BlockState;

public class BlockActivateEvent {
    public BlockState state;

    public BlockActivateEvent(BlockState state) {
        this.state = state;
    }
}
