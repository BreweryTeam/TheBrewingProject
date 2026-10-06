package dev.jsinco.brewery.bukkit.util.color;

import dev.jsinco.brewery.bukkit.util.VectorUtil;
import org.bukkit.Color;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
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
        Vector3f targetLab = toOklab(target);
        Vector3f backgroundLab = toOklab(background);

        float distance = targetLab.distance(backgroundLab);
        float alpha = Math.clamp(distance / 0.4f, 0.0f, maxOpacity / 255.0f);
        if (alpha == 0) {
            return Color.fromARGB(0, 255, 255, 255);
        }

        Vector3f blended = targetLab.mul(2.0f - alpha).sub(backgroundLab.mul(1.0f - alpha));
        return fromOklab(blended).setAlpha((int) (alpha * 255.0f));
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
        return lerp(current, destination, ratio);
    }

    public static Color lerp(Color a, Color b, float t) {
        return fromOklab(VectorUtil.lerp(toOklab(a), toOklab(b), t));
    }

    // https://bottosson.github.io/posts/oklab/
    public static Vector3f toOklab(Color color) {
        Vector3f vec = toLinearSRGB(color);
        float r = vec.x;
        float g = vec.y;
        float b = vec.z;

        float l = 0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b;
        float m = 0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b;
        float s = 0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b;

        float l_ = (float) Math.cbrt(l);
        float m_ = (float) Math.cbrt(m);
        float s_ = (float) Math.cbrt(s);

        return new Vector3f(
                0.2104542553f * l_ + 0.7936177850f * m_ - 0.0040720468f * s_,
                1.9779984951f * l_ - 2.4285922050f * m_ + 0.4505937099f * s_,
                0.0259040371f * l_ + 0.7827717662f * m_ - 0.8086757660f * s_
        );
    }

    public static Color fromOklab(Vector3fc oklab) {
        float L = oklab.x();
        float a = oklab.y();
        float b = oklab.z();

        float l_ = L + 0.3963377774f * a + 0.2158037573f * b;
        float m_ = L - 0.1055613458f * a - 0.0638541728f * b;
        float s_ = L - 0.0894841775f * a - 1.2914855480f * b;

        float l = l_ * l_ * l_;
        float m = m_ * m_ * m_;
        float s = s_ * s_ * s_;

        Vector3f vec = new Vector3f(
                4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s,
                -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s,
                -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s
        );
        return fromLinearSRGB(vec);
    }

    private static Vector3f toLinearSRGB(Color color) {
        return new Vector3f(
                f_inv(color.getRed() / 255.0f),
                f_inv(color.getGreen() / 255.0f),
                f_inv(color.getBlue() / 255.0f)
        );
    }
    private static Color fromLinearSRGB(Vector3f vec) {
        return Color.fromRGB(toIntScale(f(vec.x)), toIntScale(f(vec.y)), toIntScale(f(vec.z)));
    }
    private static int toIntScale(float f) {
        return Math.clamp((int) (255.0f * f), 0, 255);
    }

    // https://bottosson.github.io/posts/colorwrong/
    private static float f(float c) {
        return c >= 0.0031308f
                ? 1.055f * (float) Math.pow(c, 1.0f / 2.4f) - 0.055f
                : 12.92f * c;
    }
    private static float f_inv(float c) {
        return c >= 0.04045
                ? (float) Math.pow((c + 0.055f) / 1.055f, 2.4f)
                : c / 12.92f;
    }

    public static java.awt.Color getDistinctColor(BufferedImage image) {
        int imageSize = 0;
        Bucket[] buckets = new Bucket[32];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new Bucket();
        }
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                java.awt.Color pixel = new java.awt.Color(image.getRGB(x, y), true);
                if (pixel.getAlpha() < 32) {
                    continue;
                }
                float[] hsb = new float[3];
                java.awt.Color.RGBtoHSB(pixel.getRed(), pixel.getGreen(), pixel.getBlue(), hsb);
                float h = hsb[0];
                float s = hsb[1];
                float b = hsb[2];
                if (b < 0.2F) {
                    continue;
                }
                imageSize++;
                int bucketsIndex = Math.clamp((int) (h * buckets.length), 0, buckets.length);
                buckets[bucketsIndex].add(h, s, b);
            }
        }
        int minCount = imageSize >> 3;
        return Arrays.stream(buckets)
                .filter(bucket -> bucket.count >= minCount)
                .max(Comparator.comparingDouble(Bucket::weightedScore))
                .map(Bucket::average)
                .orElse(java.awt.Color.GRAY);
    }

    static class Bucket {
        private float hSum = 0F;
        private float sSum = 0F;
        private float bSum = 0F;
        private int count = 0;


        void add(float h, float s, float b) {
            this.hSum += h;
            this.sSum += s;
            this.bSum += b;
            count++;
        }

        java.awt.Color average() {
            if (count == 0) {
                return java.awt.Color.GRAY;
            }
            return java.awt.Color.getHSBColor(hSum / count, sSum / count, bSum / count);
        }

        double weightedScore() {
            if (count == 0) {
                return 0;
            }
            float s = sSum / count;
            float b = bSum / count;
            return Math.sqrt(count) * s * s * b;
        }

    }
}
