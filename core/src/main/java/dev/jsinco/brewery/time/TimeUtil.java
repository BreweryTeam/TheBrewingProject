package dev.jsinco.brewery.time;

import dev.jsinco.brewery.configuration.Config;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TimeUtil {

    private TimeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final Pattern ALLOWED_PATTERN = Pattern.compile(
            "^((\\d+t)|(\\d+s)|(\\d+min)|(\\d+h)|(\\d+d)|(\\d+w)|(\\d+y)|\\d+cmin|\\d+ay| )+$"
    );
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(|\\.\\d+)");

    public static long parse(String duration) {
        return parse(duration, TimeUnit.MINUTES);
    }

    public static long parse(String duration, TimeUnit prioritizedUnit) {
        if (NUMBER_PATTERN.matcher(duration).matches()) {
            return (long) (Double.parseDouble(duration) * prioritizedUnit.value());
        }
        if (!ALLOWED_PATTERN.matcher(duration).matches()) {
            return 0L;
        }
        // Could do some more argument validation, but meh
        return TimeUnit.ALL.stream()
                .map(unit -> parseLong(unit.pattern(), duration) * unit.value())
                .reduce(Long::sum)
                .orElse(0L);
    }

    public static String minimalString(long aLong) {
        return minimalString(aLong, null, Set.of(TimeUnit.AGING_YEARS, TimeUnit.COOKING_MINUTES));
    }

    public static boolean validTime(String time) {
        return ALLOWED_PATTERN.matcher(time).matches() || NUMBER_PATTERN.matcher(time).matches();
    }

    public static String minimalString(long aLong, @Nullable TimeUnit prioritizedUnit, Set<TimeUnit> banned) {
        if (aLong <= 0) {
            return "0" + (prioritizedUnit == null ? "t" : prioritizedUnit.suffix());
        }
        long currentValue = aLong;
        StringBuilder builder = new StringBuilder();
        // Try to use the prioritized unit whenever possible without getting too verbose
        if (prioritizedUnit != null && currentValue % prioritizedUnit.value() == 0) {
            long amount = currentValue / prioritizedUnit.value();
            if (amount <= 99) {
                currentValue = currentValue - prioritizedUnit.value() * amount;
                builder.append(amount).append(prioritizedUnit.suffix()).append(" ");
            }
        }
        for (int i = TimeUnit.ALL.size() - 1; i >= 0; i--) {
            TimeUnit unit = TimeUnit.ALL.get(i);
            if (currentValue < unit.value() || banned.contains(unit)) {
                continue;
            }
            long unitAmount = currentValue / unit.value();
            currentValue = currentValue % unit.value();
            builder.append(unitAmount).append(unit.suffix()).append(" ");
        }
        return builder.deleteCharAt(builder.length() - 1).toString();
    }

    private static long parseLong(Pattern timeUnitPattern, String text) {
        Matcher matcher = timeUnitPattern.matcher(text);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        return 0;
    }

    public record TimeUnit(Pattern pattern, Supplier<Long> valueSupplier, String suffix) {

        public static final TimeUnit TICKS = new TimeUnit("(\\d+)t", 1, "t");
        public static final TimeUnit SECONDS = TICKS.relative("(\\d+)s", 20, "s");
        public static final TimeUnit MINUTES = SECONDS.relative("(\\d+)min", 60, "min");
        public static final TimeUnit HOURS = MINUTES.relative("(\\d+)h", 60, "h");
        public static final TimeUnit DAYS = HOURS.relative("(\\d+)d", 24, "d");
        public static final TimeUnit WEEKS = DAYS.relative("(\\d+)w", 7, "w");
        public static final TimeUnit YEARS = DAYS.relative("(\\d+)y", 365, "y");
        public static final TimeUnit COOKING_MINUTES = new TimeUnit("(\\d+)cmin", () -> Config.config().cauldrons().cookingMinuteTicks(), "cmin");
        public static final TimeUnit AGING_YEARS = new TimeUnit("(\\d+)ay", () -> Config.config().barrels().agingYearTicks(), "ay");

        public static final List<TimeUnit> ALL = Stream.of(
                        TICKS,
                        SECONDS,
                        MINUTES,
                        HOURS,
                        DAYS,
                        WEEKS,
                        YEARS,
                        COOKING_MINUTES,
                        AGING_YEARS
                ).sorted(Comparator.comparing(TimeUnit::value))
                .toList();

        private TimeUnit(@RegExp String regex, long value, String suffix) {
            this(Pattern.compile(regex), () -> value, suffix);
        }

        private TimeUnit(@RegExp String regex, Supplier<Long> valueSupplier, String suffix) {
            this(Pattern.compile(regex), valueSupplier, suffix);
        }

        private TimeUnit relative(@RegExp String pattern, long multiplier, String suffix) {
            return new TimeUnit(pattern, this.value() * multiplier, suffix);
        }

        private long value() {
            return valueSupplier.get();
        }
    }
}
