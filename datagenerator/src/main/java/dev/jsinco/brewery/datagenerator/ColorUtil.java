package dev.jsinco.brewery.datagenerator;

import java.awt.*;
import java.awt.image.BufferedImage;

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
}
