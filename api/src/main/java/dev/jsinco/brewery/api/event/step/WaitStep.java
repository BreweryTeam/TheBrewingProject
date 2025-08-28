package dev.jsinco.brewery.api.event.step;

import dev.jsinco.brewery.api.event.EventStepProperty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record WaitStep(int durationTicks) implements EventStepProperty {

    private static final Pattern TICKS_PATTERN = Pattern.compile("(\\d+)t");
    private static final Pattern SECONDS_PATTERN = Pattern.compile("(\\d+)s");
    private static final Pattern MINUTES_PATTERN = Pattern.compile("(\\d+)m");
    private static final Pattern HOURS_PATTERN = Pattern.compile("(\\d+)h");
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("[tsmhdwy0-9 ]+");
    // For the laughs
    private static final Pattern DAYS_PATTERN = Pattern.compile("(\\d+)d");
    private static final Pattern WEEKS_PATTERN = Pattern.compile("(\\d+)w");
    private static final Pattern YEARS_PATTERN = Pattern.compile("(\\d+)y");

    private static final int SECONDS = 20; // TICKS
    private static final int MINUTES = SECONDS * 60;
    private static final int HOURS = MINUTES * 60;
    private static final int DAYS = HOURS * 24;
    private static final int WEEKS = DAYS * 7;
    private static final int YEARS = DAYS * 365;

    public static WaitStep parse(String duration) {
        if (!ALLOWED_CHARACTERS.matcher(duration).matches()) {
            throw new IllegalArgumentException("Invalid duration argument: " + duration);
        }
        // Could do some more argument validation, but meh
        return new WaitStep(
                parseInteger(TICKS_PATTERN, duration)
                        + parseInteger(SECONDS_PATTERN, duration) * SECONDS
                        + parseInteger(MINUTES_PATTERN, duration) * MINUTES
                        + parseInteger(HOURS_PATTERN, duration) * HOURS
                        + parseInteger(DAYS_PATTERN, duration) * DAYS
                        + parseInteger(WEEKS_PATTERN, duration) * WEEKS
                        + parseInteger(YEARS_PATTERN, duration) * YEARS
        );
    }

    private static int parseInteger(Pattern timeUnitPattern, String text) {
        Matcher matcher = timeUnitPattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}
