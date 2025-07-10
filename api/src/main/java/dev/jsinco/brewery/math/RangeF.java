package dev.jsinco.brewery.math;

import java.util.Random;

public record RangeF(float min, float max) {
    private static final Random RANDOM = new Random();

    /**
     * Returns a random number between mix and max
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
     * <p>
     * Examples:
     *     <ul>
     *         <li><code>-3.5;6.1</code> min: -3.5, max: 6.1</li>
     *         <li><code>1.0</code> min: 1.0, max: 1.0</li>
     *     </ul>
     * </p>
     */
    public static RangeF fromString(String str) {
        String[] parts = str.trim().split(";");
        if (parts.length == 0 || parts.length > 2) {
            throw new IllegalArgumentException("Invalid range");
        }

        return new RangeF(
                Float.parseFloat(parts[0]),
                parts.length == 2 ? Float.parseFloat(parts[1]) : Float.parseFloat(parts[0])
        );
    }
}
