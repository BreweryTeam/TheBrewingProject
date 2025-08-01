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
            return parseString(string);
        }
        if (value instanceof Integer integer) {
            return new Interval(integer, integer);
        }
        throw new IllegalArgumentException("Illegal value: " + value);
    }

    public static Interval parseString(@NotNull String string) {
        if (string.contains(";")) {
            String[] split = string.split(";");
            return new Interval(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        }
        if (!string.contains("-")) {
            int i = Integer.parseInt(string);
            return new Interval(i, i);
        }
        String[] split = string.split("-");
        return new Interval(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    @Override
    public @NotNull String toString() {
        return start + ";" + stop;
    }

    public String asString() {
        if (start == stop) {
            return String.valueOf(start);
        }
        return String.format("%d;%d", start, stop);
    }
}
