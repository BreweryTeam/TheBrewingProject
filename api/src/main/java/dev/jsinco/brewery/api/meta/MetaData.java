package dev.jsinco.brewery.api.meta;

import net.kyori.adventure.key.Key;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A basic metadata container, and the primitive type for nested metadata containers ({@link MetaDataType#CONTAINER}).
 */
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
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public MetaData withoutMeta(Key key) {
        return new MetaData(meta.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(key))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public <P, C> C meta(Key key, MetaDataType<P, C> type) {
        Object value = meta.get(key);
        if (value == null) {
            return null;
        }
        if (type.getPrimitiveType().isInstance(value)) {
            return type.toComplex(type.getPrimitiveType().cast(value));
        }
        throw new IllegalArgumentException("Meta for " + key + " is not of type " + type.getPrimitiveType().getSimpleName());
    }

    /**
     * @return An unmodifiable map of keys to metadata values as their primitive types
     */
    public Map<Key, Object> primitiveMap() {
        return meta;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MetaData metaData && Objects.equals(meta, metaData.meta);
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(meta);
    }

    @Override
    public String toString() {
        return meta.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ", "MetaData{", "}"));
    }

}
