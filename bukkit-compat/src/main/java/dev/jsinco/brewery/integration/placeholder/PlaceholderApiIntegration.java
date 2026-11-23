package dev.jsinco.brewery.integration.placeholder;

import dev.jsinco.brewery.api.util.ClassUtil;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import dev.jsinco.brewery.bukkit.api.integration.PlaceholderIntegration;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public record PlaceholderApiIntegration(TheBrewingProjectApi api, Plugin owner) implements PlaceholderIntegration {
    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("me.clip.placeholderapi.expansion.PlaceholderExpansion");
    }

    @Override
    public String getId() {
        return "placeholderapi";
    }

    @Override
    public void onEnable() {
        new PlaceholderApiExpansion(api, owner).register();
    }

    @Override
    public TagResolver resolve(OfflinePlayer player) {
        return TagResolver.resolver("placeholderapi", ((argumentQueue, context) -> {
            String placeholder = argumentQueue.popOr("missing_placeholder").value();
            return Tag.selfClosingInserting(Component.text(PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%")));
        }));
    }
}
