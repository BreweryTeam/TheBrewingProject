package dev.jsinco.brewery.vector;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

public record BreweryLocation(int x, int y, int z, UUID worldUuid) {

    public BreweryVector toVector() {
        return new BreweryVector(x, y, z);
    }

    public BreweryLocation add(int x, int y, int z) {
        return new BreweryLocation(x + x(), y + y(), z + z(), worldUuid);
    }

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
