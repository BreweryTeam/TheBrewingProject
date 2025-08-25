package dev.jsinco.brewery.util;

import java.util.UUID;

public interface Holder<T> {

    /**
     * @return The value contained in this holder
     */
    T value();

    record Material(BreweryKey value) implements Holder<BreweryKey> {

        public static Material fromMinecraftId(String key) {
            return new Material(new BreweryKey("minecraft", key));
        }
    }

    record Player(UUID value) implements Holder<UUID> {
    }

    record World(UUID value) implements Holder<UUID> {
    }
}
