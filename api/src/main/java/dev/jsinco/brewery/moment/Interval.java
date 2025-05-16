package dev.jsinco.brewery.moment;

import org.jetbrains.annotations.NotNull;

public record Interval(long start, long stop) implements Moment {

    @Override
    public long moment() {
        return stop - start;
    }

    @Override
    public Interval withLastStep(long lastStep) {
        return new Interval(start, lastStep);
    }

    @Override
    public Interval withMovedEnding(long newEnd) {
        return new Interval(newEnd - stop + start, newEnd);
    }

    public static Interval parse(@NotNull Object value) throws IllegalArgumentException {
        if (value instanceof String string) {
            if (!string.contains("-")) {
                int i = Integer.parseInt(string);
                return new Interval(i, i);
            }
            String[] split = string.split("-");
            return new Interval(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        } else if (value instanceof Integer integer) {
            return new Interval(integer, integer);
        } else {
            throw new IllegalArgumentException("Illegal value: " + value);
        }
    }

    @Override
    public String toString() {
        return start + "-" + stop;
    }
}
