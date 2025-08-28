package dev.jsinco.brewery.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Utility class with minimal pattern requirements. Always lower case
 *
 * @param namespace A namespace (can not contain ":")
 * @param key       A key
 */
public record BreweryKey(String namespace, String key) {

    /**
     * Defaults to brewery namespace.
     *
     * @param input A string
     * @return A new brewery key from the parsed string
     * @throws IllegalArgumentException If the key somehow was invalid
     */
    public static @NotNull BreweryKey parse(@NotNull String input) {
        return parse(input, "brewery");
    }

    /**
     * @param input            A string
     * @param defaultNamespace The default namespace
     * @return A new brewery key from the parsed string given the default namespace
     * @throws IllegalArgumentException If the key somehow was invalid
     */
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
    public @NotNull String toString() {
        return namespace + ":" + key;
    }
}
