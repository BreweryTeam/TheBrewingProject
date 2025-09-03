package dev.jsinco.brewery.bukkit.integration.placeholder;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.PlaceholderIntegration;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.util.ClassUtil;
import io.github.miniplaceholders.api.Expansion;
import io.github.miniplaceholders.api.MiniPlaceholders;
import io.github.miniplaceholders.api.utils.TagsUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class MiniPlaceholdersIntegration implements PlaceholderIntegration {

    @Override
    public TagResolver resolve(OfflinePlayer player) {
        if (player instanceof Player onlinePlayer) {
            return MiniPlaceholders.getAudiencePlaceholders(onlinePlayer);
        }
        return TagResolver.empty();
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("io.github.miniplaceholders.api.utils.TagsUtils");
    }

    @Override
    public String getId() {
        return "miniplaceholders";
    }

    @Override
    public void onEnable() {
        Expansion.Builder builder = Expansion.builder("tbp")
                .filter(Player.class)
                .audiencePlaceholder("next_event", (audience, queue, ctx) -> {
                    if (!(audience instanceof Player player)) {
                        return TagsUtils.EMPTY_TAG;
                    }
                    Pair<DrunkEvent, Long> event = TheBrewingProject.getInstance().getDrunksManager().getPlannedEvent(player.getUniqueId());
                    Component eventComponent = event == null ? Component.translatable("tbp.events.nothing-planned") : event.first().displayName();
                    return Tag.selfClosingInserting(eventComponent);
                });
        for (DrunkenModifier modifier : DrunkenModifierSection.modifiers().drunkenModifiers()) {
            builder.audiencePlaceholder(modifier.name(), (audience, argumentQueue, context) -> {
                if (!(audience instanceof Player player)) {
                    return TagsUtils.EMPTY_TAG;
                }
                DrunkState drunkState = TheBrewingProject.getInstance().getDrunksManager().getDrunkState(player.getUniqueId());
                return Tag.selfClosingInserting(Component.text(drunkState == null ? 0 : drunkState.modifierValue(modifier)));
            });
        }
        builder.build().register();
    }
}
