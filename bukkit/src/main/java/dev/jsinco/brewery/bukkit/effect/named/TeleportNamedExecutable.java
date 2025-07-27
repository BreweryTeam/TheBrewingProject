package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.event.EventPropertyExecutable;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.util.MessageUtil;
import dev.jsinco.brewery.vector.BreweryLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
        List<UUID> worldUuids = Bukkit.getWorlds().stream().map(World::getUID).toList();
        List<BreweryLocation> locations = Config.config().events().teleportDestinations().stream().map(uncompiledLocation ->
                uncompiledLocation.get(worldUuids)
        ).filter(Objects::nonNull).toList();
        if (locations.isEmpty()) {
            return ExecutionResult.CONTINUE;
        }
        BreweryLocation teleport = locations.get(RANDOM.nextInt(locations.size()));
        Location location = BukkitAdapter.toLocation(teleport);
        player.teleportAsync(location);
        MessageUtil.msg(player, TranslationsConfig.TELEPORT_MESSAGE, BukkitMessageUtil.getPlayerTagResolver(player));
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
