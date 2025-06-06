package dev.jsinco.brewery.bukkit.integration.placeholder;

import dev.jsinco.brewery.bukkit.integration.PlaceholderIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlaceholderApiIntegration implements PlaceholderIntegration {
    @Override
    public boolean enabled() {
        return ClassUtil.exists("me.clip.placeholderapi.expansion.PlaceholderExpansion");
    }

    @Override
    public String getId() {
        return "placeholderapi";
    }

    @Override
    public void initialize() {
        new PlaceholderApiExpansion().register();
    }

    @Override
    public String process(String input, OfflinePlayer player) {
        if (player instanceof Player onlinePlayer) {
            return PlaceholderAPI.setPlaceholders(onlinePlayer, input);
        }
        return PlaceholderAPI.setPlaceholders(player, input);
    }

    @Override
    public TagResolver resolve(OfflinePlayer player) {
        return TagResolver.empty();
    }
}
