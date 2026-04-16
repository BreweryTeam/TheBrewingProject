package dev.jsinco.brewery.api.math;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record RangeD(Double min, Double max) {

    private static final char INFINITY = '∞';

    public RangeD {
        Preconditions.checkArgument(min != null || max != null, "Expected at least one bound");
        Preconditions.checkArgument(min == null || max == null || min <= max, "Expected a smaller value for lower bound");
    }

    public boolean isWithin(double value) {
        if (min != null && min > value) {
            return false;
        }
        return max == null || max >= value;
    }

    public boolean isOutside(double aDouble) {
        return !isWithin(aDouble);
    }

    @Override
    public @NonNull String toString() {
        StringBuilder builder = new StringBuilder();
        if (min == null) {
            builder.append('-');
            builder.append(INFINITY);
        } else {
            builder.append(min);
        }
        builder.append(';');
        if (max == null) {
            builder.append(INFINITY);
        } else {
            builder.append(max);
        }
        return builder.toString();
    }

    public static RangeD fromString(String string) {
        String[] split = string.split(";");
        Preconditions.checkArgument(split.length == 2, "Expected exactly one ';' for range");
        Double min;
        if (split[0].equalsIgnoreCase("-" + INFINITY)) {
            min = null;
        } else {
            min = Double.parseDouble(split[0]);
        }
        Double max;
        if (split[1].equalsIgnoreCase(String.valueOf(INFINITY))) {
            max = null;
        } else {
            max = Double.parseDouble(split[1]);
        }
        return new RangeD(min, max);
    }
}
