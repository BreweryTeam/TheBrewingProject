package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.event.ExecutableEventStep;
import dev.jsinco.brewery.util.MessageUtil;
import dev.jsinco.brewery.vector.BreweryLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class TeleportNamedExecutable implements ExecutableEventStep {


    @Override
    public void execute(UUID contextPlayer, List<EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return;
        }

        List<BreweryLocation> locations = Config.config().events().teleportDestinations().stream().map(Supplier::get).toList();
        if (locations.isEmpty()) {
            return;
        }
        BreweryLocation teleport = locations.get(RANDOM.nextInt(locations.size()));
        Location location = BukkitAdapter.toLocation(teleport);
        player.teleportAsync(location);
        MessageUtil.msg(player, TranslationsConfig.TELEPORT_MESSAGE, BukkitMessageUtil.getPlayerTagResolver(player));
    }

}
