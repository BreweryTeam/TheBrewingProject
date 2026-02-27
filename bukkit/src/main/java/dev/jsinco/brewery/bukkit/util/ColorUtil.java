package dev.jsinco.brewery.bukkit.util;

import org.bukkit.Color;

import java.util.HashMap;
import java.util.Map;

public class ColorUtil {


    public static final Map<String, Color> NAME_TO_COLOR_MAP = new HashMap<>();

    static {
        NAME_TO_COLOR_MAP.put("WHITE", Color.WHITE);
        NAME_TO_COLOR_MAP.put("SILVER", Color.SILVER);
        NAME_TO_COLOR_MAP.put("GRAY", Color.GRAY);
        NAME_TO_COLOR_MAP.put("BLACK", Color.BLACK);
        NAME_TO_COLOR_MAP.put("RED", Color.RED);
        NAME_TO_COLOR_MAP.put("MAROON", Color.MAROON);
        NAME_TO_COLOR_MAP.put("YELLOW", Color.YELLOW);
        NAME_TO_COLOR_MAP.put("OLIVE", Color.OLIVE);
        NAME_TO_COLOR_MAP.put("LIME", Color.LIME);
        NAME_TO_COLOR_MAP.put("GREEN", Color.GREEN);
        NAME_TO_COLOR_MAP.put("AQUA", Color.AQUA);
        NAME_TO_COLOR_MAP.put("TEAL", Color.TEAL);
        NAME_TO_COLOR_MAP.put("BLUE", Color.BLUE);
        NAME_TO_COLOR_MAP.put("NAVY", Color.NAVY);
        NAME_TO_COLOR_MAP.put("FUCHSIA", Color.FUCHSIA);
        NAME_TO_COLOR_MAP.put("PURPLE", Color.PURPLE);
        NAME_TO_COLOR_MAP.put("ORANGE", Color.ORANGE);
        NAME_TO_COLOR_MAP.put("PINK", Color.FUCHSIA);
        NAME_TO_COLOR_MAP.put("BRIGHT_GRAY", Color.SILVER);
        NAME_TO_COLOR_MAP.put("BRIGHT_RED", Color.fromRGB(255, 0, 0));
        NAME_TO_COLOR_MAP.put("DARK_RED", Color.fromRGB(128, 0, 0));
    }

    public static Color closestColorLimitedOpacity(Color target, Color background, int maxOpacity) {
        int r = Math.abs(target.getRed() - background.getRed());
        int g = Math.abs(target.getGreen() - background.getGreen());
        int b = Math.abs(target.getBlue() - background.getBlue());

        int a = Math.min(maxOpacity, Math.max(r, Math.max(g, b)));
        return Color.fromARGB(
                a,
                calculateColor(target.getRed(), background.getRed(), a),
                calculateColor(target.getGreen(), background.getGreen(), a),
                calculateColor(target.getBlue(), background.getBlue(), a)
        );
    }

    private static int calculateColor(int colorBand, int backgroundBand, int alpha) {
        if (alpha == 0) {
            return 255;
        }
        int modifiedBand = 2 * colorBand * alpha - backgroundBand * (255 - alpha);
        return Math.max(0, Math.min(255, modifiedBand / 255));
    }

    public static Color parseColorString(String hexOrValue) {
        hexOrValue = hexOrValue.replace("&", "").replace("#", "").toUpperCase();
        if (NAME_TO_COLOR_MAP.containsKey(hexOrValue)) {
            return NAME_TO_COLOR_MAP.get(hexOrValue);
        }
        try {
            return Color.fromRGB(
                    Integer.valueOf(hexOrValue.substring(0, 2), 16),
                    Integer.valueOf(hexOrValue.substring(2, 4), 16),
                    Integer.valueOf(hexOrValue.substring(4, 6), 16));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid color string: " + hexOrValue);
        }
    }

    // Returns a color closer to the destination color based on the interval and totalDuration
    public static Color getNextColor(Color current, Color destination, long step, long duration) {
        float ratio = Math.min((float) step / (duration - 1), 1f);
        int red = (int) (ratio * destination.getRed() + (1f - ratio) * current.getRed());
        int green = (int) (ratio * destination.getGreen() + (1f - ratio) * current.getGreen());
        int blue = (int) (ratio * destination.getBlue() + (1f - ratio) * current.getBlue());

        return Color.fromRGB(red, green, blue);
    }
}
