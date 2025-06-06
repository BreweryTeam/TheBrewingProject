package dev.jsinco.brewery.bukkit.integration;

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;

public interface PlaceholderIntegration extends Integration {

    String process(String input, OfflinePlayer player);

    TagResolver resolve(OfflinePlayer player);
}
