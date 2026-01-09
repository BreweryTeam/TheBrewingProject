package dev.jsinco.brewery.api.meta;

import com.google.errorprone.annotations.Immutable;
import net.kyori.adventure.key.Key;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A basic metadata container, and the primitive type for nested metadata containers ({@link MetaDataType#CONTAINER}).
 * Not suitable for use as a key in a hash-based collection.
 */
@Immutable
public final class MetaData implements MetaContainer<MetaData> {

    private final Map<Key, Object> meta;

    /**
     * Creates an empty metadata container.
     */
    public MetaData() {
        this(Collections.emptyMap());
    }
    private MetaData(Map<Key, Object> meta) {
        this.meta = meta;
    }

    @Override
    public <P, C> MetaData withMeta(Key key, MetaDataType<P, C> type, C value) {
        return new MetaData(Stream.concat(
                meta.entrySet().stream(),
                Stream.of(Map.entry(key, type.toPrimitive(value)))
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue)));
    }

    @Override
    public MetaData withoutMeta(Key key) {
        return new MetaData(meta.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(key))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public MetaData meta() {
        return this;
    }

    @Override
    public <P, C> C meta(Key key, MetaDataType<P, C> type) {
        Object value = meta.get(key);
        if (value == null) {
            return null;
        }
        if (!type.getPrimitiveType().isInstance(value)) {
            throw new IllegalArgumentException("Meta for " + key + " is not of type " + type.getPrimitiveType().getSimpleName());
        }
        if (type instanceof ListMetaDataType<?, ?> listType) {
            List<?> list = (List<?>) value;
            if (!list.isEmpty() && !listType.getElementDataType().getPrimitiveType().isInstance(list.getFirst())) {
                throw new IllegalArgumentException("Meta for " + key + " is not of List with element type " +
                        listType.getElementDataType().getPrimitiveType().getSimpleName());
            }
        }
        return type.toComplex(type.getPrimitiveType().cast(value));
    }

    @Override
    public <P, C> boolean hasMeta(Key key, MetaDataType<P, C> type) {
        Object value = meta.get(key);
        if (value == null) {
            return false;
        }
        if (!type.getPrimitiveType().isInstance(value)) {
            return false;
        }
        if (type instanceof ListMetaDataType<?,?> listType) {
            List<?> list = (List<?>) value;
            return list.isEmpty() || listType.getElementDataType().getPrimitiveType().isInstance(list.getFirst());
        }
        return true;
    }

    @Override
    public Set<Key> metaKeys() {
        return meta.keySet();
    }

    /**
     * Gets the raw metadata mapping. This method is mainly meant to help with serialization,
     * prefer the type-safe {@link #meta(Key, MetaDataType)} method instead.
     * @return An unmodifiable map of keys to metadata values as their primitive types
     */
    public Map<Key, Object> primitiveMap() {
        return meta;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MetaData metaData && areMapsEqual(meta, metaData.meta);
    }

    // Version of map.equals that properly checks for array equality
    private static <K, V> boolean areMapsEqual(Map<K, V> map1, Map<K, V> map2) {
        return map1.size() == map2.size() && map1.entrySet().stream()
                .allMatch(entry -> areEqual(entry.getValue(), map2.get(entry.getKey())));
    }
    private static boolean areListsEqual(List<?> list1, List<?> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            if (!areEqual(list1.get(i), list2.get(i))) {
                return false;
            }
        }
        return true;
    }
    private static boolean areEqual(Object obj1, Object obj2) {
        if (obj1 instanceof byte[] arr1 && obj2 instanceof byte[] arr2) {
            return Arrays.equals(arr1, arr2);
        }
        if (obj1 instanceof int[] arr1 && obj2 instanceof int[] arr2) {
            return Arrays.equals(arr1, arr2);
        }
        if (obj1 instanceof long[] arr1 && obj2 instanceof long[] arr2) {
            return Arrays.equals(arr1, arr2);
        }
        if (obj1 instanceof List<?> list1 && obj2 instanceof List<?> list2) {
            return areListsEqual(list1, list2);
        }
        return obj1.equals(obj2);
    }

    @Override
    public String toString() {
        return meta.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + asString(entry.getValue()))
                .collect(Collectors.joining(", ", "MetaData{", "}"));
    }
    private String asString(Object value) {
        return switch (value) {
            case byte[] arr -> Arrays.toString(arr);
            case int[] arr -> Arrays.toString(arr);
            case long[] arr -> Arrays.toString(arr);
            case List<?> list -> list.stream().map(this::asString).collect(Collectors.joining(", "));
            default -> value.toString();
        };
    }

}
