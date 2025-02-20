package me.bebeli555.automapart.utils;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;

public class WorldUtils extends Utils {
    /**
     * Gets all the loaded chunks in the current world
     */
    public static List<WorldChunk> getLoadedChunks() {
        List<WorldChunk> list = new ArrayList<>();
        for (int i = 0; i < mc.world.getChunkManager().chunks.chunks.length(); i++) {
            WorldChunk chunk = mc.world.getChunkManager().chunks.chunks.get(i);
            if (chunk != null) {
                list.add(chunk);
            }
        }

        return list;
    }

    /**
     * Gets the ip of the current server or Singleplayer
     */
    public static String getServerIp() {
        try {
            return mc.world.getServer().getServerIp();
        } catch (Exception e) {
            return "Singleplayer";
        }
    }

    /**
     * Checks if this position is in a loaded chunk or not
     */
    public static boolean isInLoadedChunk(BlockPos pos) {
        return getLoadedChunks().stream().anyMatch(c -> c.getPos().x == pos.getX() / 16 && c.getPos().z == pos.getZ() / 16);
    }
}
