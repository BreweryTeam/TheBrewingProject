package dev.jsinco.brewery.api.brew;


public enum BrewQuality {
    BAD(0xFF0000),
    GOOD(0xFFA500),
    EXCELLENT(0x00FF00);

    private final int color;

    BrewQuality(int color) {
        this.color = color;
    }

    /**
     * @return An integer with RGB color representing this quality
     */
    public int getColor() {
        return color;
    }
}
