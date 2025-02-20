package me.bebeli555.automapart.utils;

import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchematicReader {
    public List<SchematicBlock> list = new ArrayList<>();

    public void load(File file) throws Exception {
        NbtCompound rootTag = NbtIo.readCompressed(file.toPath(), NbtSizeTracker.ofUnlimitedBytes());

        NbtList palette = rootTag.getList("palette", 10);
        NbtList blocks = rootTag.getList("blocks", 10);

        int xAdd = Integer.MAX_VALUE;
        int zAdd = Integer.MAX_VALUE;

        for (int i = 0; i < blocks.size(); i++) {
            NbtCompound block = blocks.getCompound(i);
            NbtList posList = block.getList("pos", 3);
            int x = posList.getInt(0);
            int z = posList.getInt(2);

            if (xAdd == Integer.MAX_VALUE) {
                xAdd = -x;
                zAdd = -z;
            }

            SchematicBlock schematicBlock = new SchematicBlock();
            schematicBlock.pos = new BlockPos(x + xAdd, 0, z + zAdd);

            int state = block.getInt("state");
            schematicBlock.block = Registries.BLOCK.get(new Identifier(palette.getCompound(state).getString("Name")));

            list.add(schematicBlock);
        }
    }

    public static class SchematicBlock {
        public Block block;
        public BlockPos pos;

        @Override
        public String toString() {
            return "Pos: " + (pos == null ? "null" : pos.toString()) + " block: " + (block == null ? "null" : block.toString());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SchematicBlock) {
                SchematicBlock schematicBlock = (SchematicBlock)obj;
                return schematicBlock.block == block && schematicBlock.pos.equals(pos);
            }

            return false;
        }
    }
}
