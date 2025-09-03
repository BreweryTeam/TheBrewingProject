package dev.jsinco.brewery.api.moment;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * An interval with start and stop time
 *
 * @param start Time of start
 * @param stop  Time of stop
 */
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

    /**
     * @param value An integer or a string
     * @return The interval of the parsed value
     * @throws IllegalArgumentException If the contents could not be read
     */
    public static Interval parse(@NotNull Object value) throws IllegalArgumentException {
        if (value instanceof String string) {
            return parseString(string);
        }
        if (value instanceof Integer integer) {
            return new Interval(integer, integer);
        }
        throw new IllegalArgumentException("Illegal value: " + value);
    }

    /**
     * @param string A string
     * @return The interval of parsed string
     * @throws IllegalArgumentException If the contents could not be read
     */
    public static Interval parseString(@NotNull String string) throws IllegalArgumentException {
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

    /**
     * @return The interval as a string
     */
    public String asString() {
        if (start == stop) {
            return String.valueOf(start);
        }
        return String.format("%d;%d", start, stop);
    }

    public @NotNull Component displayName() {
        if (start == stop) {
            return Component.text(start);
        }
        return Component.text("[" + start + ", " + stop + "]");
    }
}
