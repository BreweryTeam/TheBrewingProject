package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.integration.Integration;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface PlaceholderIntegration extends Integration {

    /**
     * Provides a TagResolver that replaces placeholders for the given OfflinePlayer
     */
    TagResolver resolve(OfflinePlayer player);
}
