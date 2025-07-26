package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.event.EventPropertyExecutable;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.vector.BreweryLocation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class TeleportExecutable implements EventPropertyExecutable {

    private final BreweryLocation.Uncompiled location;

    public TeleportExecutable(BreweryLocation.Uncompiled location) {
        this.location = location;
    }

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }
        List<UUID> worldUuids = Bukkit.getWorlds().stream().map(World::getUID).toList();
        BreweryLocation breweryLocation = location.get(worldUuids);
        if (breweryLocation == null) {
            return ExecutionResult.CONTINUE;
        }
        player.teleportAsync(BukkitAdapter.toLocation(breweryLocation));
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return 0;
    }

}
