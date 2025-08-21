package dev.jsinco.brewery.bukkit.integration.placeholder;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.effect.DrunkState;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.effect.DrunksManager;
import dev.jsinco.brewery.event.DrunkEvent;
import dev.jsinco.brewery.util.Pair;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            drunkState = new DrunkStateImpl(0, 0, TheBrewingProject.getInstance().getTime(), -1);
        }
        return switch (params) {
            case "alcohol" -> String.valueOf(drunkState.alcohol());
            case "toxins" -> String.valueOf(drunkState.toxins());
            case "next_event" -> {
                Pair<DrunkEvent, Long> event = drunksManager.getPlannedEvent(player.getUniqueId());
                if (event == null) {
                    yield PlainTextComponentSerializer.plainText().serialize(Component.translatable("tbp.events.nothing-planned"));
                }
                yield PlainTextComponentSerializer.plainText().serialize(event.first().displayName());
            }
            default -> null;
        };
    }
}
