package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.api.BukkitAdapter;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.bukkit.util.LocationUtil;
import dev.jsinco.brewery.configuration.EventSection;
import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.util.MessageUtil;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TeleportNamedExecutable implements EventPropertyExecutable {


    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }
        List<BreweryLocation> locations = EventSection.events().teleportDestinations().stream().flatMap(uncompiledLocation ->
                uncompiledLocation.stream(LocationUtil::resolveWorld)
        ).filter(Objects::nonNull).toList();
        if (locations.isEmpty()) {
            return ExecutionResult.CONTINUE;
        }
        BreweryLocation teleport = locations.get(RANDOM.nextInt(locations.size()));
        BukkitAdapter.toLocation(teleport)
                .ifPresent(location -> {
                    player.teleportAsync(location);
                    MessageUtil.message(player, "tbp.events.teleport-message", BukkitMessageUtil.getPlayerTagResolver(player));
                });
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
