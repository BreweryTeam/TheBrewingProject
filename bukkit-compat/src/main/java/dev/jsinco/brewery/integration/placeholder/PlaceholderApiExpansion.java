package dev.jsinco.brewery.integration.placeholder;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.DrunksManager;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PlaceholderApiExpansion extends PlaceholderExpansion {

    private final TheBrewingProjectApi api;
    private final Plugin owner;

    public PlaceholderApiExpansion(TheBrewingProjectApi api, Plugin owner) {
        this.api = api;
        this.owner = owner;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "tbp";
    }

    @Override
    public @NonNull String getAuthor() {
        return String.join(",", owner.getPluginMeta().getAuthors());
    }

    @Override
    public @NonNull String getVersion() {
        return owner.getPluginMeta().getVersion();
    }

    @Override
    public String onRequest(@Nullable OfflinePlayer player, @NonNull String params) {
        return readPlayer(player, params);
    }

    @Override
    public String onPlaceholderRequest(@Nullable Player player, @NonNull String params) {
        return readPlayer(player, params);
    }

    private String readPlayer(OfflinePlayer player, String params) {
        if (player == null) {
            return null;
        }
        DrunksManager drunksManager = api.getDrunksManager();
        DrunkState drunkState = drunksManager.getDrunkState(player.getUniqueId());
        for (DrunkenModifier modifier : api.getModifierManager().allModifiers()) {
            if (params.equals(modifier.name())) {
                return String.valueOf(drunkState == null ?
                        modifier.minValue()
                        : drunkState.modifierValue(modifier));
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
