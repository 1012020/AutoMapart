package me.bebeli555.automapart.utils;

import me.bebeli555.automapart.gui.Gui;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class for all kinds of different utilities
 */
public class Utils {
    public static MinecraftClient mc = MinecraftClient.getInstance();

    private static final HashMap<String, Block> blockMap = Registries.BLOCK.stream().collect(Collectors.toMap(b -> b.getName().getString(), Function.identity(), (a, b) -> b, HashMap::new));
    private static final HashMap<String, Item> itemMap = Registries.ITEM.stream().collect(Collectors.toMap(i -> i.getName().getString(), Function.identity(), (a, b) -> b, HashMap::new));
    public static final Direction[] SIDE_DIRECTIONS = {Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH};

    /**
     * Generates random number between min and max
     */
    public static int random(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    /**
     * Suspends the given thread
     */
    @SuppressWarnings("removal")
    public static void suspend(Thread thread) {
        if (thread != null) thread.suspend();
    }

    /**
     * Splits the input into 2 strings at the last occurrence of prefix
     */
    public static String[] getLastSplit(String input, String prefix) {
        int i = input.lastIndexOf(prefix);
        try {
            return new String[]{input.substring(0, i), input.substring(i + 1)};
        } catch (Exception ignored) {
            return new String[]{"", ""};
        }
    }

    /**
     * Queries and returns the HTML from a web request to the given url
     */
    public static String getHtml(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/116.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            return response.toString();
        } catch (Exception e) {
            //e.printStackTrace();
            return "";
        }
    }

    /**
     * Generates a unique color for the hash with the given alpha
     */
    public static Color generateColorFromHash(int hashCode, int alpha) {
        int positiveHashCode = Math.abs(hashCode);

        int red = (positiveHashCode >> 16) & 0xFF;
        int green = (positiveHashCode >> 8) & 0xFF;
        int blue = positiveHashCode & 0xFF;

        return new Color(red, green, blue, alpha);
    }

    /**
     * Finds a block with the given name, which is fetched from HashMap so performance is good
     */
    public static Block getBlockFromName(String name) {
        return blockMap.get(name);
    }

    /**
     * Finds an item with the given name, which is fetched from HashMap so performance is good
     */
    public static Item getItemFromName(String name) {
        return itemMap.get(name);
    }

    /**
     * Appends the given string array with the given item
     */
    public static String[] addToArray(String[] myArray, String newItem) {
        int currentSize = myArray.length;
        int newSize = currentSize + 1;
        String[] tempArray = new String[ newSize ];
        System.arraycopy(myArray, 0, tempArray, 0, currentSize);
        tempArray[newSize- 1] = newItem;

        return tempArray;
    }

    /**
     * Sleeps the current thread with the given milliseconds
     */
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get only the decimals from a double
     */
    public static double getDecimals(double d) {
        return d - (int)d;
    }

    /**
     * Creates a list where all the string element widths are the given width or lower
     */
    public static List<String> getStringListThatFitsIntoWidth(MatrixStack stack, String string, int width) {
        List<String> list = new ArrayList<>();
        String current = "";
        for (String space : string.split(" ")) {
            if (Gui.fontRenderer.getWidth(stack, current + " " + space) >= width - 2) {
                list.add(current);
                current = space;
            } else {
                current += (current.isEmpty() ? "" : " ") + space;
            }
        }

        list.add(current);
        return list;
    }

    /**
     * Sleeps the current thread sleepAmount every iteration until the given condition is met
     */
    public static void sleepUntil(BooleanSupplier condition, int timeout, int sleepAmount) {
        long startTime = System.currentTimeMillis();
        while (!condition.getAsBoolean() && (timeout == -1 || System.currentTimeMillis() - startTime < timeout)) {
            sleep(sleepAmount);
        }
    }

    /**
     * Sleeps the thread until the condition is met, every sleep cycle sleeps 10 milliseconds
     */
    public static void sleepUntil(BooleanSupplier condition, int timeout) {
        sleepUntil(condition, timeout, 10);
    }

    /**
     * Converts vector into a string
     */
    public static String vecToString(Vec3d vec) {
        return vec.x + "," + vec.y + "," + vec.z;
    }

    /**
     * Parses a Vec3d from a string that was converted with vecToString
     */
    public static Vec3d vecFromString(String s) {
        String[] split = s.split(",");
        return new Vec3d(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
    }
}
