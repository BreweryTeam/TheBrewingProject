package dev.jsinco.brewery.math;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public abstract class Range<T extends Number> {
    protected static final Random RANDOM = new Random();

    private final @NotNull T min;
    private final @NotNull T max;

    public Range(@NotNull T min, @NotNull T max) {
        this.min = Objects.requireNonNull(min, "min cannot be null");
        this.max = Objects.requireNonNull(max, "max cannot be null");
    }

    public @NotNull T getMin() {
        return min;
    }

    public @NotNull T getMax() {
        return max;
    }

    /**
     * Returns a random number between mix and max
     */
    public abstract T getRandom();
}
