package dev.jsinco.brewery.bukkit.api.integration;

import dev.jsinco.brewery.api.integration.Integration;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;

public interface PlaceholderIntegration extends Integration {

    /**
     * Provides a TagResolver that replaces placeholders for the given OfflinePlayer
     */
    TagResolver resolve(OfflinePlayer player);
}
