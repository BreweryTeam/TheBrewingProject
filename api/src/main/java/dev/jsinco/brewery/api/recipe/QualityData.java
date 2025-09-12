package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.brew.BrewQuality;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility for handling quality data
 *
 * @param <T> The type of the data
 */
public class QualityData<T> {

    private final Map<BrewQuality, T> backing;
    private static final Pattern TAG_PATTERN = Pattern.compile("^[!?#]?[a-z0-9_-]*>|^[!?#]?[a-z0-9_-]*:.*>");

    private QualityData(Map<BrewQuality, T> backing) {
        this.backing = backing;
    }

    public static <T> QualityData<T> equalValued(T t) {
        return new QualityData<>(Arrays.stream(BrewQuality.values())
                .collect(Collectors.toMap(quality -> quality, ignored -> t)));
    }

    public <U> QualityData<U> map(Function<T, U> mapper) {
        Map<BrewQuality, U> newBacking = new HashMap<>();
        for (Map.Entry<BrewQuality, T> entry : backing.entrySet()) {
            newBacking.put(entry.getKey(), mapper.apply(entry.getValue()));
        }
        return new QualityData<>(newBacking);
    }

    /**
     * @param brewQuality Quality data
     * @return The data for specified
     */
    public @Nullable T get(BrewQuality brewQuality) {
        return backing.get(brewQuality);
    }

    public static <T> QualityData<T> fromValueMapper(Function<BrewQuality, T> mapper) {
        return new QualityData<>(Arrays.stream(BrewQuality.values())
                .collect(Collectors.toMap(quality -> quality, mapper)));
    }

    public static QualityData<String> readQualityFactoredString(@Nullable String string) {
        if (string == null) {
            return new QualityData<>(Map.of());
        }
        String[] list = split(string);
        if (list.length == 1) {
            return new QualityData<>(Map.of(BrewQuality.BAD, string, BrewQuality.GOOD, string, BrewQuality.EXCELLENT, string));
        }
        if (list.length != 3) {
            throw new IllegalArgumentException("Expected a string with format <bad>/<good>/<excellent>");
        }
        Map<BrewQuality, String> map = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            map.put(BrewQuality.values()[i], list[i]);
        }
        return new QualityData<>(map);
    }

    private static String[] split(String string) {
        int previous = 0;
        Stream.Builder<String> builder = Stream.builder();
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (character != '/') {
                continue;
            }
            if (i == 0 || i == string.length() - 1) {
                builder.add(string.substring(previous, i));
                previous = i + 1;
                continue;
            }
            if (string.charAt(i - 1) == '<' && TAG_PATTERN.matcher(string.substring(i + 1)).find()) {
                continue;
            }
            builder.add(string.substring(previous, i));
            previous = i + 1;
        }
        builder.add(string.substring(previous));
        return builder.build()
                .toArray(String[]::new);
    }

    public static QualityData<List<String>> readQualityFactoredStringList(@Nullable List<String> stringList) {
        if (stringList == null) {
            return new QualityData<>(Arrays.stream(BrewQuality.values())
                    .collect(Collectors.toMap(brewQuality -> brewQuality, ignored -> List.of())));
        }
        Map<BrewQuality, List<String>> map = new HashMap<>();

        for (String string : stringList) {
            if (string.startsWith("+++")) {
                map.computeIfAbsent(BrewQuality.EXCELLENT, ignored -> new ArrayList<>()).add(string.substring(3));
            } else if (string.startsWith("++")) {
                map.computeIfAbsent(BrewQuality.GOOD, ignored -> new ArrayList<>()).add(string.substring(2));
            } else if (string.startsWith("+")) {
                map.computeIfAbsent(BrewQuality.BAD, ignored -> new ArrayList<>()).add(string.substring(1));
            } else {
                for (BrewQuality quality : BrewQuality.values()) {
                    map.computeIfAbsent(quality, ignored -> new ArrayList<>()).add(string);
                }
            }
        }
        for (BrewQuality quality : BrewQuality.values()) {
            map.putIfAbsent(quality, new ArrayList<>());
        }
        return new QualityData<>(map);
    }

    public T getOrDefault(BrewQuality quality, T t) {
        return backing.getOrDefault(quality, t);
    }

    public void forEach(BiConsumer<BrewQuality, T> consumer) {
        for (BrewQuality brewQuality : BrewQuality.values()) {
            T value = get(brewQuality);
            if (value == null) {
                continue;
            }
            consumer.accept(brewQuality, value);
        }
    }

    public <U> QualityData<U> qualityMap(BiFunction<BrewQuality, T, U> biFunction) {
        Map<BrewQuality, U> newBacking = new HashMap<>();
        for (Map.Entry<BrewQuality, T> entry : backing.entrySet()) {
            newBacking.put(entry.getKey(), biFunction.apply(entry.getKey(), entry.getValue()));
        }
        return new QualityData<>(newBacking);
    }

    public static String toQualityFactoredString(QualityData<String> qualityData) {
        return qualityData.get(BrewQuality.BAD) + "/" + qualityData.get(BrewQuality.GOOD) + "/" + qualityData.get(BrewQuality.EXCELLENT);
    }
}
