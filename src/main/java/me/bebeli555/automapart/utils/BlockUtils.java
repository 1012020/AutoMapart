package me.bebeli555.automapart.utils;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.ClientTickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockUtils extends Mod {
    /**
     * Gets all the BlockPositions around the given vector that are inside the given radius,
     * and optionally sorts the list by proximity to the given vector.
     */
    public static List<BlockPos> getAll(Vec3d around, int radius, boolean sort) {
        List<BlockPos> list = new ArrayList<>();
        for (int x = (int) (around.x - radius); x < around.x + radius; x++) {
            for (int z = (int) (around.z - radius); z < around.z + radius; z++) {
                for (int y = (int) (around.y + radius); y > around.y - radius; y--) {
                    list.add(new BlockPos(x, y, z));
                }
            }
        }

        if (sort) {
            list.sort(Comparator.comparingDouble(lhs -> around.squaredDistanceTo(lhs.getX(), lhs.getY(), lhs.getZ())));
        }

        return list;
    }

    /**
     * Searches the radius around the given vector and returns the first BlockPos that contains the given block,
     * or null if not found.
     */
    public static BlockPos findBlock(Vec3d around, Block block, int radius) {
        return getAll(around, radius, true).stream()
                .filter(pos -> getBlock(pos) == block)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the Manhattan distance between two BlockPos objects.
     */
    public static int distance(BlockPos first, BlockPos second) {
        return Math.abs(first.getX() - second.getX())
                + Math.abs(first.getY() - second.getY())
                + Math.abs(first.getZ() - second.getZ());
    }
    
    /**
     * Returns the squared Euclidean distance between two BlockPos objects.
     */
    public static double distanceSquared(BlockPos a, BlockPos b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        int dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Returns the Manhattan distance from the client player to the given BlockPos.
     */
    public static double distanceToPlayer(BlockPos pos) {
        return Math.abs(mc.player.getPos().x - pos.getX())
                + Math.abs(mc.player.getPos().y - pos.getY())
                + Math.abs(mc.player.getPos().z - pos.getZ());
    }

    /**
     * Returns a Vec3d representing the given BlockPos.
     */
    public static Vec3d toVec(BlockPos pos) {
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Returns a BlockPos from the given Vec3d.
     */
    public static BlockPos fromVec(Vec3d vec) {
        return new BlockPos((int) vec.x, (int) vec.y, (int) vec.z);
    }

    /**
     * Checks if the block at the given BlockPos is solid.
     */
    public static boolean isSolid(BlockPos pos) {
        return mc.world.getBlockState(pos).isSolidBlock(mc.world, pos);
    }

    /**
     * Returns the block at the given BlockPos.
     */
    public static Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    /**
     * Checks if a block can be placed at the given location.
     * If ignoreSelf is true, then the client player is ignored in the check.
     */
    public static boolean canPlaceAt(BlockPos pos, boolean ignoreSelf) {
        return mc.world.getBlockState(pos).canPlaceAt(mc.world, pos);
    }

    /**
     * Returns a unique color for the block at the given BlockPos with the specified alpha.
     */
    public static Color getUniqueColor(BlockPos pos, int alpha) {
        MapColor mapColor = mc.world.getBlockState(pos).getMapColor(mc.world, pos);
        if (getBlock(pos) == Blocks.NETHER_PORTAL) {
            mapColor = MapColor.PURPLE;
        } else if (mapColor == MapColor.CLEAR) {
            return null;
        }

        int c = mapColor.getRenderColor(MapColor.Brightness.NORMAL);
        Color color = new Color(c);
        // Swap red and blue channels as needed, then set the alpha.
        color = new Color(color.getBlue(), color.getGreen(), color.getRed(), alpha);

        return color;
    }

    /**
     * Returns a box representing only the specified side of the given box.
     */
    public static Box getSideBox(Box box, Direction side) {
        if (side == Direction.WEST) {
            box = new Box(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ);
        } else if (side == Direction.NORTH) {
            box = new Box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
        } else if (side == Direction.EAST) {
            box = new Box(box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.minZ);
        } else if (side == Direction.SOUTH) {
            box = new Box(box.maxX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ);
        } else if (side == Direction.DOWN) {
            box = new Box(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
        } else if (side == Direction.UP) {
            box = new Box(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
        }
        return box;
    }

    /**
     * Returns a bounding box for the block at the given position,
     * based on its voxel shape and including the block position offset.
     */
    public static Box getBbFromPos(BlockPos pos) {
        VoxelShape shape = mc.world.getBlockState(pos).getOutlineShape(mc.world, pos);
        if (shape.isEmpty()) {
            return null;
        }

        Box temp = shape.getBoundingBox();
        return new Box(temp.minX + pos.getX(), temp.minY + pos.getY(), temp.minZ + pos.getZ(),
                temp.maxX + pos.getX(), temp.maxY + pos.getY(), temp.maxZ + pos.getZ());
    }
}
