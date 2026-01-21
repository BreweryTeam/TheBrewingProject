package dev.jsinco.brewery.api.meta;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface MetaContainer<SELF extends MetaContainer<SELF>> {

    /**
     * Creates a new meta container with the provided metadata added. The old container is unchanged.
     * @param key The key the value will be stored under
     * @param type The type of the value
     * @param value The value to store
     * @return A new meta container
     * @param <P> The primitive type the value will be stored as when serialized
     * @param <C> The type of the value to store
     */
    <P, C> SELF withMeta(Key key, MetaDataType<P, C> type, C value);

    /**
     * Creates a new meta container with the provided metadata removed. The old container is unchanged.
     * @param key The key to remove
     * @return A new meta container
     */
    SELF withoutMeta(Key key);

    /**
     * Gets all metadata stored in this container.
     * @return All metadata
     */
    MetaData meta();

    /**
     * Gets metadata under the provided key.
     * @param key The key to look up
     * @param type The type of the metadata value
     * @return The metadata value, or null if the key is not present
     * @param <P> The primitive type the value is stored as when serialized
     * @param <C> The type of the value to retrieve
     * @throws IllegalArgumentException If the value is not of the expected type
     */
    <P, C> @Nullable C meta(Key key, MetaDataType<P, C> type);

    /**
     * Checks if this container has metadata of the specified type under the provided key.
     * @param key The key to look up
     * @param type The type of the metadata value
     * @return True if {@link #meta(Key, MetaDataType)} will return a value
     * @param <P> The primitive type the value is stored as when serialized
     * @param <C> The type of the value to retrieve
     */
    <P, C> boolean hasMeta(Key key, MetaDataType<P, C> type);

    /**
     * Gets all keys in this container.
     * @return An immutable set of keys
     */
    Set<Key> metaKeys();

}
