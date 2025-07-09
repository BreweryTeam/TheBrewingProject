package dev.jsinco.brewery.math;

import java.util.Random;

public abstract class Range<T extends Number> {
    protected static final Random RANDOM = new Random();

    private T min;
    private T max;

    public Range(T min, T max) {
        this.min = min;
        this.max = max;
    }

    public Range(String str) {
        fromString(str);
    }

    public T getMin() {
        return min;
    }

    public void setMin(T min) {
        this.min = min;
    }

    public T getMax() {
        return max;
    }

    public void setMax(T max) {
        this.max = max;
    }

    protected abstract void fromString(String str);

    public abstract T getRandom();
}
