package me.bebeli555.automapart.events.block;

import net.minecraft.block.Block;

public class BlockSlipperinessEvent {
    public Block block;
    public float slipperiness;

    public BlockSlipperinessEvent(Block block, float slipperiness) {
        this.block = block;
        this.slipperiness = slipperiness;
    }
}
