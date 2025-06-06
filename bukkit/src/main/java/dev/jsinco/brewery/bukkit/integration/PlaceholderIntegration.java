package dev.jsinco.brewery.bukkit.integration;

import org.bukkit.OfflinePlayer;

public interface PlaceholderIntegration extends Integration {

    String process(String input, OfflinePlayer player);
}
