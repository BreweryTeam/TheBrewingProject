package dev.jsinco.brewery.vector;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @param x         Position
 * @param y         Position
 * @param z         Position
 * @param worldUuid UUID of world
 */
public record BreweryLocation(int x, int y, int z, UUID worldUuid) {

    /**
     * @return The location converted to a vector
     */
    public BreweryVector toVector() {
        return new BreweryVector(x, y, z);
    }

    /**
     * @param x X to add
     * @param y Y to add
     * @param z Z to add
     * @return A new brewery location with modified coordinates
     */
    public BreweryLocation add(int x, int y, int z) {
        return new BreweryLocation(x + x(), y + y(), z + z(), worldUuid);
    }

    @ApiStatus.Internal
    public record Uncompiled(int x, int y, int z, String worldIdentifier) {

        public Optional<BreweryLocation> get(Function<String, @Nullable UUID> worldUuidFunction) {
            return Optional.ofNullable(worldUuidFunction.apply(worldIdentifier))
                    .map(worldUuid -> new BreweryLocation(x, y, z, worldUuid));
        }

        /**
         * Utility method if you want to convert the object in a stream (Stream#flatMap)
         *
         * @param worldUuidFunction
         * @return
         */
        public Stream<BreweryLocation> stream(Function<String, @Nullable UUID> worldUuidFunction) {
            return get(worldUuidFunction).stream();
        }

    }

}
