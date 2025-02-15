package me.bebeli555.automapart.utils.font;

import java.awt.*;

public class ColorHolder {
    public static String startChar = "§";
    public static String endChar = "½";

    public int r;
    public int g;
    public int b;
    public int a;

    public ColorHolder(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
    }

    public ColorHolder(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public ColorHolder(Color color) {
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
        this.a = color.getAlpha();
    }

    public ColorHolder(int hex) {
        this(new Color(hex, true));
    }

    public int getRGB() {
        return new Color(r, g, b, a).getRGB();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ColorHolder other = (ColorHolder) obj;

        if (r != other.r) return false;
        if (g != other.g) return false;
        if (b != other.b) return false;
        return a == other.a;
    }

    @Override
    public int hashCode() {
        int result = r;
        result = 31 * result + g;
        result = 31 * result + b;
        result = 31 * result + a;
        return result;
    }

    @Override
    public String toString() {
        return startChar + new Color(r, g, b, a).getRGB() + endChar;
    }
}