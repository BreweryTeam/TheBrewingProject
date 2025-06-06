package dev.jsinco.brewery.bukkit.integration.placeholder;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.integration.PlaceholderIntegration;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.effect.DrunkState;
import dev.jsinco.brewery.event.DrunkEvent;
import dev.jsinco.brewery.util.ClassUtil;
import dev.jsinco.brewery.util.Pair;
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
    public String process(String input, OfflinePlayer player) {
        return input;
    }

    @Override
    public TagResolver resolve(OfflinePlayer player) {
        if (player instanceof Player onlinePlayer) {
            return MiniPlaceholders.getAudiencePlaceholders(onlinePlayer);
        }
        return TagResolver.empty();
    }

    @Override
    public boolean enabled() {
        return ClassUtil.exists("io.github.miniplaceholders.api.utils.TagsUtils");
    }

    @Override
    public String getId() {
        return "miniplaceholders";
    }

    @Override
    public void initialize() {
        Expansion.builder("tbp")
                .audiencePlaceholder("alcohol", (audience, argumentQueue, context) -> {
                    if (!(audience instanceof Player player)) {
                        return TagsUtils.EMPTY_TAG;
                    }
                    DrunkState drunkState = TheBrewingProject.getInstance().getDrunksManager().getDrunkState(player.getUniqueId());
                    return Tag.selfClosingInserting(Component.text(drunkState == null ? 0 : drunkState.alcohol()));
                }).audiencePlaceholder("toxins", (audience, argumentQueue, context) -> {
                    if (!(audience instanceof Player player)) {
                        return TagsUtils.EMPTY_TAG;
                    }
                    DrunkState drunkState = TheBrewingProject.getInstance().getDrunksManager().getDrunkState(player.getUniqueId());
                    return Tag.selfClosingInserting(Component.text(drunkState == null ? 0 : drunkState.toxins()));
                }).audiencePlaceholder("next_event", (audience, queue, ctx) -> {
                    if (!(audience instanceof Player player)) {
                        return TagsUtils.EMPTY_TAG;
                    }
                    Pair<DrunkEvent, Long> event = TheBrewingProject.getInstance().getDrunksManager().getPlannedEvent(player.getUniqueId());
                    String eventString = event == null ? TranslationsConfig.NO_EVENT_PLANNED : event.first().displayName();
                    return Tag.selfClosingInserting(Component.text(eventString));
                }).filter(Player.class)
                .build().register();
    }
}
