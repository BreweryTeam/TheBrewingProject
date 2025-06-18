package dev.jsinco.brewery.util;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record BreweryKey(String namespace, String key) {

    /**
     * Defaults to brewery namespace.
     *
     * @param input
     * @return
     */
    public static @NotNull BreweryKey parse(@NotNull String input) {
        String[] split = input.split(":", 2); // Allow recursive namespaced keys
        if (split.length == 1) {
            return new BreweryKey("brewery", split[0].toLowerCase(Locale.ROOT));
        }
        if (split.length == 2) {
            return new BreweryKey(split[0].toLowerCase(Locale.ROOT), split[1].toLowerCase(Locale.ROOT));
        }
        throw new IllegalArgumentException("Invalid Brewery Key: " + input);
    }

    public static @NotNull BreweryKey parse(@NotNull String input, @NotNull String defaultNamespace) {
        String[] split = input.split(":", 2); // Allow recursive namespaced keys
        if (split.length == 1) {
            return new BreweryKey(defaultNamespace, split[0].toLowerCase(Locale.ROOT));
        }
        if (split.length == 2) {
            return new BreweryKey(split[0].toLowerCase(Locale.ROOT), split[1].toLowerCase(Locale.ROOT));
        }
        throw new IllegalArgumentException("Invalid Brewery Key: " + input);
    }

    @Override
    public String toString() {
        return namespace + ":" + key;
    }
}
