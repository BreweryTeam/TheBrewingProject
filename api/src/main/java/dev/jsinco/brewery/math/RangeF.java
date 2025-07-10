package dev.jsinco.brewery.math;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RangeF extends Range<Float> {
    public RangeF(@NotNull Float min, @NotNull Float max) {
        super(min, max);
    }

    @Override
    public Float getRandom() {
        if (Objects.equals(this.getMin(), this.getMax())) {
            return this.getMax();
        }
        return RANDOM.nextFloat(this.getMin(), this.getMax());
    }

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
