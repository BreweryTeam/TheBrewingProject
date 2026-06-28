package dev.jsinco.brewery.util;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class MapUtil {

    private MapUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T, U, K> Function<Map.Entry<K, T>, Map.Entry<K, U>> mapValue(Function<T, U> valueMapper) {
        return entry -> Map.entry(entry.getKey(), valueMapper.apply(entry.getValue()));
    }

    public static <T, U, V> Function<Map.Entry<T, V>, Map.Entry<U, V>> mapKey(Function<T, U> keyMapper) {
        return entry -> Map.entry(keyMapper.apply(entry.getKey()), entry.getValue());
    }

    public static <K, V> Function<Map.Entry<Optional<K>, V>, Optional<Map.Entry<K, V>>> optionalKey() {
        return entry -> entry.getKey().map(key -> Map.entry(key, entry.getValue()));
    }

    public static <K, V> Function<Map.Entry<K, Optional<V>>, Optional<Map.Entry<K, V>>> optionalValue() {
        return entry -> entry.getValue().map(value -> Map.entry(entry.getKey(), value));
    }

    public static <K> Predicate<Map.Entry<K, ?>> keyPredicate(Predicate<K> keyPredicate) {
        return entry -> keyPredicate.test(entry.getKey());
    }

    public static <V> Predicate<Map.Entry<?, V>> valuePredicate(Predicate<V> valuePredicate) {
        return entry -> valuePredicate.test(entry.getValue());
    }
}
