package dev.jsinco.brewery.api.math;

public record RangeD(Double min, Double max) {

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
