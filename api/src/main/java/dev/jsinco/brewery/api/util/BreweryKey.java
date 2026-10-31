package dev.jsinco.brewery.api.util;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

/**
 * Utility class with minimal pattern requirements.
 * Namespaces are always lower case.
 * Keys are lower case, except when the namespace is "mythic".
 *
 * @param namespace A namespace (can not contain ":")
 * @param key       A key
 */
public record BreweryKey(String namespace, String key) {

    public BreweryKey {
        namespace = namespace.toLowerCase(Locale.ROOT);
        // mythic item identifiers are case-sensitive
        if (!namespace.equals("mythic")) {
            key = key.toLowerCase(Locale.ROOT);
        }
    }

    public String minimalized() {
        return minimalized("brewery");
    }

    public String minimalized(String namespace) {
        if (this.namespace.equals(namespace)) {
            return key;
        }
        return toString();
    }

    /**
     * Defaults to brewery namespace.
     *
     * @param input A string
     * @return A new brewery key from the parsed string
     * @throws IllegalArgumentException If the key somehow was invalid
     */
    public static @NonNull BreweryKey parse(@NonNull String input) {
        return parse(input, "brewery");
    }

    /**
     * @param input            A string
     * @param defaultNamespace The default namespace
     * @return A new brewery key from the parsed string given the default namespace
     * @throws IllegalArgumentException If the key somehow was invalid
     */
    public static @NonNull BreweryKey parse(@NonNull String input, @NonNull String defaultNamespace) {
        String[] split = input.split(":", 2); // Allow recursive namespaced keys
        if (split.length == 1) {
            return new BreweryKey(defaultNamespace, split[0]);
        }
        if (split.length == 2) {
            return new BreweryKey(split[0], split[1]);
        }
        throw new IllegalArgumentException("Invalid Brewery Key: " + input);
    }

    public static BreweryKey minecraft(String value) {
        return new BreweryKey(Key.MINECRAFT_NAMESPACE, value);
    }

    @Override
    public @NonNull String toString() {
        return namespace + ":" + key;
    }
}
