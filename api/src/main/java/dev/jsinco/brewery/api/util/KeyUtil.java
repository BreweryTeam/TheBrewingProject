package dev.jsinco.brewery.api.util;

import net.kyori.adventure.key.Key;

public class KeyUtil {

    private KeyUtil(){
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Defaults to the brewery namespace
     *
     * @param key Key string with optional namespace
     * @return Namespaced key
     */
    public static Key brewery(String key) {
        if (key.contains(":")) {
            String[] split = key.split(":", 2);
            return Key.key(split[0], split[1]);
        }
        return Key.key("brewery", key);
    }

    public static String minimalize(Key key) {
        return minimalize(key, "brewery");
    }

    public static String minimalize(Key key, String namespace) {
        if(key.namespace().equals(namespace)) {
            return key.value();
        }
        return key.asString();
    }
}
