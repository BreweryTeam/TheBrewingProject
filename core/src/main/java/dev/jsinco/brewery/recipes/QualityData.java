package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.brew.BrewQuality;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QualityData<T> {

    private final Map<BrewQuality, T> backing;

    private QualityData(Map<BrewQuality, T> backing) {
        this.backing = backing;
    }

    public static <T> QualityData<T> equalValue(T t) {
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

    public T getOrDefault(BrewQuality brewQuality, T defaultValue) {
        return backing.getOrDefault(brewQuality, defaultValue);
    }

    public @Nullable T get(BrewQuality brewQuality) {
        return backing.get(brewQuality);
    }

    public static <T> QualityData<T> fromValueMapper(Function<BrewQuality, T> mapper) {
        return new QualityData<>(Arrays.stream(BrewQuality.values())
                .collect(Collectors.toMap(quality -> quality, mapper)));
    }

    public static QualityData<String> readQualityFactoredString(String string) {
        if (string == null) {
            return new QualityData<>(Map.of());
        }
        if (!string.contains("/")) {
            return new QualityData<>(Map.of(BrewQuality.BAD, string, BrewQuality.GOOD, string, BrewQuality.EXCELLENT, string));
        }

        String[] list = string.split("/");
        if (list.length != 3) {
            throw new IllegalArgumentException("Expected a string with format <bad>/<good>/<excellent>");
        }
        Map<BrewQuality, String> map = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            map.put(BrewQuality.values()[i], list[i]);
        }
        return new QualityData<>(map);
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
}
