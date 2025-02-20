package me.bebeli555.automapart.utils;

import me.bebeli555.automapart.utils.objects.Dimension;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.Arrays;

public class DimensionUtils extends Utils {
    /**
     * Gets the dimension the player is in currently
     */
    public static Dimension getDimension() {
        RegistryKey<DimensionType> dimensionKey = mc.world.getDimensionKey();
        if (dimensionKey == DimensionTypes.OVERWORLD) {
            return Dimension.OVERWORLD;
        } else if (dimensionKey == DimensionTypes.THE_NETHER) {
            return Dimension.NETHER;
        } else if (dimensionKey == DimensionTypes.THE_END) {
            return Dimension.END;
        } else {
            return Dimension.OVERWORLD;
        }
    }

    /**
     * Gets dimension from the given string
     */
    public static Dimension getDimension(String dimension) {
        return Arrays.stream(Dimension.values()).filter(d -> d.toString().equalsIgnoreCase(dimension)).findFirst().orElse(null);
    }
}
