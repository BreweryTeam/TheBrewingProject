package dev.jsinco.brewery.bukkit.integration;

public interface Integration {

    /**
     * Lowercase integration identifier
     */
    String getId();

    /**
     * Whether the integration is currently enabled
     * Usually determined by a class existing or not
     */
    boolean isEnabled();

    /**
     * Allows an integration to execute code onLoad
     */
    default void onLoad() {
    }

    /**
     * Allows an integration to execute code onEnable
     */
    default void onEnable() {
    }
}
