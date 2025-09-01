package dev.jsinco.brewery.bukkit.integration.placeholder;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.DrunksManager;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class PlaceholderApiExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "tbp";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(",", TheBrewingProject.getInstance().getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return TheBrewingProject.getInstance().getPluginMeta().getVersion();
    }

    @Override
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        return readPlayer(player, params);
    }

    @Override
    public String onPlaceholderRequest(@Nullable Player player, @NotNull String params) {
        return readPlayer(player, params);
    }

    private String readPlayer(OfflinePlayer player, String params) {
        if (player == null) {
            return null;
        }
        DrunksManager drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        DrunkState drunkState = drunksManager.getDrunkState(player.getUniqueId());
        if (drunkState == null) {
            drunkState = new DrunkStateImpl(TheBrewingProject.getInstance().getTime(), -1, DrunkenModifierSection.modifiers()
                    .drunkenModifiers().stream()
                    .collect(Collectors.toUnmodifiableMap(modifier -> modifier, DrunkenModifier::minValue)));
        }
        for (DrunkenModifier modifier : DrunkenModifierSection.modifiers().drunkenModifiers()) {
            if (params.equals(modifier.name())) {
                return String.valueOf(drunkState.modifierValue(modifier.name()));
            }
        }
        if (params.equals("next_event")) {
            Pair<DrunkEvent, Long> event = drunksManager.getPlannedEvent(player.getUniqueId());
            if (event == null) {
                return PlainTextComponentSerializer.plainText().serialize(Component.translatable("tbp.events.nothing-planned"));
            }
            return PlainTextComponentSerializer.plainText().serialize(event.first().displayName());
        }
        return null;
    }
}
