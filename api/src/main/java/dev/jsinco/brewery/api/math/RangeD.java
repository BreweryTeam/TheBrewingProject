package dev.jsinco.brewery.api.math;

import com.google.common.base.Preconditions;

public record RangeD(Double min, Double max) {

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
}
