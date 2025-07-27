package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.integration.IntegrationType;
import dev.jsinco.brewery.bukkit.integration.PlaceholderIntegration;
import dev.jsinco.brewery.bukkit.recipe.RecipeEffects;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.stream.Collectors;

public class BukkitMessageUtil {

    private BukkitMessageUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static TagResolver recipeEffectResolver(RecipeEffects effects) {
        return TagResolver.resolver(
                Placeholder.component("potion_effects", effects.getEffects().stream()
                        .map(effect ->
                                Component.translatable(effect.type().translationKey())
                                        .append(Component.text("/" + effect.durationRange() + "/" + effect.amplifierRange()))
                        ).collect(Component.toComponent(Component.text(",")))
                ),
                Formatter.number("effect_alcohol", effects.getAlcohol()),
                Formatter.number("effect_toxins", effects.getToxins()),
                Placeholder.parsed("effect_title_message", effects.getTitle() == null ? "" : effects.getTitle()),
                Placeholder.parsed("effect_message", effects.getMessage() == null ? "" : effects.getMessage()),
                Placeholder.parsed("effect_action_bar", effects.getActionBar() == null ? "" : effects.getActionBar()),
                Placeholder.parsed("effect_events", effects.getEvents().stream().map(drunkEvent -> {
                    if (drunkEvent instanceof NamedDrunkEvent namedDrunkEvent) {
                        return TranslationsConfig.EVENT_TYPES.get(namedDrunkEvent.displayName().toLowerCase(Locale.ROOT));
                    }
                    return drunkEvent.displayName();
                }).collect(Collectors.joining(",")))
        );
    }

    public static Component compilePlayerMessage(String message, Player player, DrunksManagerImpl<?> drunksManager, int alcohol) {
        DrunkStateImpl drunkState = drunksManager.getDrunkState(player.getUniqueId());
        return MessageUtil.miniMessage(
                preProcessPlayerMessage(message, player),
                Placeholder.parsed("alcohol", String.valueOf(alcohol)),
                Placeholder.parsed("player_alcohol", String.valueOf(drunkState == null ? "0" : drunkState.alcohol())),
                getPlayerTagResolver(player)
        );
    }

    public static TagResolver getPlayerTagResolver(Player player) {
        return TagResolver.resolver(
                Placeholder.component("player_name", player.name()),
                Placeholder.component("team_name", player.teamDisplayName()),
                Placeholder.unparsed("world", player.getWorld().getName()),
                TagResolver.resolver(
                        TheBrewingProject.getInstance().getIntegrationManager().retrieve(IntegrationType.PLACEHOLDER).stream()
                                .map(placeholderIntegration -> placeholderIntegration.resolve(player))
                                .toArray(TagResolver[]::new)
                )
        );
    }

    private static String preProcessPlayerMessage(String message, Player player) {
        String modifiedMessage = message;
        for (PlaceholderIntegration placeholderIntegration : TheBrewingProject.getInstance().getIntegrationManager().retrieve(IntegrationType.PLACEHOLDER)) {
            modifiedMessage = placeholderIntegration.process(modifiedMessage, player);
        }
        return modifiedMessage;
    }
}
