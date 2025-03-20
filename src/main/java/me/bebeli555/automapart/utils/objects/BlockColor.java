package me.bebeli555.automapart.utils.objects;

import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class BlockColor {
    public BlockPos pos;
    public Color color;
    public int lineWidth;

    public BlockColor(BlockPos pos, Color color, int lineWidth) {
        this.pos = pos;
        this.color = color;
        this.lineWidth = lineWidth;
    }

    public BlockColor(BlockPos pos, Color color) {
        this.pos = pos;
        this.color = color;
    }
}
