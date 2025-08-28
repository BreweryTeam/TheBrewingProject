package dev.jsinco.brewery.api.math;

import java.util.Random;

/**
 * @param min The minimum value of the range
 * @param max The maximum value of the range
 */
public record RangeF(float min, float max) {
    private static final Random RANDOM = new Random();

    public RangeF {
        if (this.max() < this.min()) {
            throw new IllegalArgumentException("Range max needs to be larger than range min");
        }
    }

    /**
     * Returns a random number between min and max
     */
    public Float getRandom() {
        if (this.min() == this.max()) {
            return this.max();
        }
        return RANDOM.nextFloat(this.min(), this.max());
    }

    /**
     * <p>
     * Creates a RangeF object from a string, accepted formats are <code>&lt;min&gt;;&lt;max&gt;</code> or just <code>&lt;min_and_max&gt;</code>
     * </p>
     * <p>Examples:</p>
     * <ul>
     *     <li><code>-3.5;6.1</code> min: -3.5, max: 6.1</li>
     *     <li><code>1.0</code> min: 1.0, max: 1.0</li>
     * </ul>
     */
    public static RangeF fromString(String str) {
        String[] parts = str.trim().split(";");
        if (parts.length > 2) {
            throw new IllegalArgumentException("Invalid range");
        }

        return new RangeF(
                Float.parseFloat(parts[0]),
                parts.length == 2 ? Float.parseFloat(parts[1]) : Float.parseFloat(parts[0])
        );
    }
}
