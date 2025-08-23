package dev.jsinco.brewery.bukkit.integration.placeholder;

import dev.jsinco.brewery.bukkit.integration.PlaceholderIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
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
    public void enable() {
        new PlaceholderApiExpansion().register();
    }

    @Override
    public TagResolver resolve(OfflinePlayer player) {
        return TagResolver.resolver("placeholderapi", ((argumentQueue, context) -> {
            String placeholder = argumentQueue.popOr("missing_placeholder").value();
            return Tag.selfClosingInserting(Component.text(PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%")));
        }));
    }
}
