package dev.jsinco.brewery.bukkit.integration;

public interface Integration {
    boolean enabled();

    String getId();

    void initialize();
}
