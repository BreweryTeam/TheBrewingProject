package dev.jsinco.brewery.bukkit.integration;

public interface Integration {
    boolean enabled();

    String getId();

    default void load() {
    }

    void enable();
}
