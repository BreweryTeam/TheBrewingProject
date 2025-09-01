package dev.jsinco.brewery.bukkit.util;

import com.mojang.brigadier.Message;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.recipe.RecipeEffects;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class BukkitMessageUtil {

    private BukkitMessageUtil() {
        throw new IllegalStateException("Utility class");
    }


    public static Message toBrigadier(String translation, TagResolver... resolvers) {
        return MessageComponentSerializer.message().serialize(Component.translatable(translation, Argument.tagResolver(resolvers)));
    }

    public static TagResolver recipeEffectResolver(RecipeEffects effects) {
        TagResolver.Builder builder = TagResolver.builder()
                .resolver(Placeholder.component("potion_effects", effects.getEffects().stream()
                        .map(effect ->
                                Component.translatable(effect.type().translationKey())
                                        .append(Component.text("/" + effect.durationRange() + "/" + effect.amplifierRange()))
                        )
                        .collect(Component.toComponent(Component.text(",")))
                ))
                .resolver(Placeholder.parsed("effect_title_message", effects.getTitle() == null ? "" : effects.getTitle()))
                .resolver(Placeholder.parsed("effect_message", effects.getMessage() == null ? "" : effects.getMessage()))
                .resolver(Placeholder.parsed("effect_action_bar", effects.getActionBar() == null ? "" : effects.getActionBar()))
                .resolver(Placeholder.component("effect_events", effects.getEvents().stream()
                        .map(DrunkEvent::displayName)
                        .collect(Component.toComponent(Component.text(", ")))
                ))
                .resolver(MessageUtil.numberedModifierTagResolver(effects.getModifiers(), "effect"));
        return builder.build();
    }

    public static TagResolver getPlayerTagResolver(@Nullable OfflinePlayer offlinePlayer) {
        TagResolver.Builder output = TagResolver.builder();
        if (offlinePlayer instanceof Player player) {
            output.resolver(Placeholder.component("player_name", player.name()));
        } else {
            String offlineName = offlinePlayer == null ? null : offlinePlayer.getName();
            output.resolver(Placeholder.unparsed("player_name", offlineName != null ? offlineName : "unknown_player"));
        }
        if (offlinePlayer == null) {
            return output.build();
        }
        output.resolver(TagResolver.resolver(
                TheBrewingProject.getInstance().getIntegrationManager().retrieve(IntegrationTypes.PLACEHOLDER).stream()
                        .map(placeholderIntegration -> placeholderIntegration.resolve(offlinePlayer))
                        .toArray(TagResolver[]::new)
        ));
        return output.build();
    }
}
