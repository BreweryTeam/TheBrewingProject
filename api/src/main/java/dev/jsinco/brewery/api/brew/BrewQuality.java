package dev.jsinco.brewery.api.brew;


import org.jspecify.annotations.Nullable;

import java.util.Optional;

public enum BrewQuality {
    BAD(0xFF0000),
    GOOD(0xFFA500),
    EXCELLENT(0x00FF00);

    private final int color;

    BrewQuality(int color) {
        this.color = color;
    }

    /**
     * @param overrideQuality The quality of the score, null is failed
     * @return the maximum score for each quality
     */
    public static double maxScore(@Nullable BrewQuality overrideQuality) {
        return switch (overrideQuality) {
            case BAD -> 0.6D - Math.ulp(0.6D);
            case GOOD -> 0.8D - Math.ulp(0.8D);
            case EXCELLENT -> 1D;
            case null -> 0.0;
        };
    }

    /**
     * @param score A brew score between 0-1
     * @return The quality of the score, or empty if score is failed
     */
    public static Optional<BrewQuality> quality(double score) {
        if (score >= 0.8) {
            return Optional.of(BrewQuality.EXCELLENT);
        }
        if (score >= 0.6) {
            return Optional.of(BrewQuality.GOOD);
        }
        if (score > 0) {
            return Optional.of(BrewQuality.BAD);
        }
        return Optional.empty();
    }

    /**
     * @return An integer with RGB color representing this quality
     */
    public int getColor() {
        return color;
    }

    public String colorKey() {
        return "tbp.brew.quality-color." + name().toLowerCase();
    }
}