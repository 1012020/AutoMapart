package me.bebeli555.automapart.utils.font;

import java.util.Arrays;

public enum DyeColors {
    BLACK('0', new ColorHolder(0, 0, 0)),
    RED('c', new ColorHolder(250, 32, 32)),
    GREEN('a', new ColorHolder(32, 250, 32)),
    BROWN('6', new ColorHolder(180, 100, 48)),
    BLUE('9', new ColorHolder(48, 48, 255)),
    PURPLE('5', new ColorHolder(137, 50, 184)),
    CYAN('b', new ColorHolder(64, 230, 250)),
    LIGHT_GRAY('7', new ColorHolder(160, 160, 160)),
    GRAY('8', new ColorHolder(80, 80, 80)),
    PINK('d', new ColorHolder(255, 128, 172)),
    LIME('a', new ColorHolder(132, 240, 32)),
    YELLOW('e', new ColorHolder(255, 232, 0)),
    LIGHT_BLUE('b', new ColorHolder(100, 160, 255)),
    MAGENTA('d', new ColorHolder(220, 64, 220)),
    ORANGE('6', new ColorHolder(255, 132, 32)),
    WHITE('f', new ColorHolder(255, 255, 255)),
    DARK_AQUA('3', new ColorHolder(0, 170, 170));

    private final ColorHolder color;
    private final char code;

    DyeColors(char code, ColorHolder color) {
        this.code = code;
        this.color = color;
    }

    public ColorHolder getColor() {
        return color;
    }

    public char getChar() {
        return code;
    }

    public static ColorHolder getColorFromChar(char code) {
        return Arrays.stream(DyeColors.values()).filter(c -> c.getChar() == code).toList().get(0).getColor();
    }
}