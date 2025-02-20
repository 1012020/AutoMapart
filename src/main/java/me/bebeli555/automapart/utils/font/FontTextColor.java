package me.bebeli555.automapart.utils.font;

import java.util.ArrayList;
import java.util.List;

public class FontTextColor {
    public List<TextColor> list = new ArrayList<>();

    public FontTextColor(String s, int startColor) {
        s = new ColorHolder(startColor) + s;
        for (String split : s.split(ColorHolder.startChar)) {
            int splitColor = -1;
            if (split.contains(ColorHolder.endChar)) {
                splitColor = Integer.parseInt(split.split(ColorHolder.endChar)[0]);
                try {
                    split = split.split(ColorHolder.endChar)[1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
            } else {
                try {
                    splitColor = DyeColors.getColorFromChar(split.charAt(0)).getRGB();
                    split = split.substring(1);
                } catch (Exception ignored) {}
            }

            if (split.isEmpty()) {
                continue;
            }

            list.add(new TextColor(split, splitColor));
        }
    }

    public record TextColor(String text, int color) {}
}
