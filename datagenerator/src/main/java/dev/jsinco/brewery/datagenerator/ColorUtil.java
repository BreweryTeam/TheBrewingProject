package dev.jsinco.brewery.datagenerator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;

public class ColorUtil {

    public static Color getAverageColor(BufferedImage image) {
        long sumr = 0, sumg = 0, sumb = 0;
        double num = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixel = new Color(image.getRGB(x, y), true);
                float alpha = (float) pixel.getAlpha() / 255;
                sumr += (long) (pixel.getRed() * alpha);
                sumg += (long) (pixel.getGreen() * alpha);
                sumb += (long) (pixel.getBlue() * alpha);
                num += alpha;
            }
        }
        return new Color((int) (sumr / num), (int) (sumg / num), (int) (sumb / num));
    }

    public static Color getDistinctColor(BufferedImage image) {
        int imageSize = 0;
        Bucket[] buckets = new Bucket[32];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new Bucket();
        }
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixel = new Color(image.getRGB(x, y), true);
                if (pixel.getAlpha() < 32) {
                    continue;
                }
                float[] hsb = new float[3];
                Color.RGBtoHSB(pixel.getRed(), pixel.getGreen(), pixel.getBlue(), hsb);
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
                .orElse(Color.GRAY);
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

        Color average() {
            if (count == 0) {
                return Color.GRAY;
            }
            return Color.getHSBColor(hSum / count, sSum / count, bSum / count);
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
