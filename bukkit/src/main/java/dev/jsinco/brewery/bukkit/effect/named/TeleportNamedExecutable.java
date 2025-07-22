package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.event.EventPropertyExecutable;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.vector.BreweryLocation;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class TeleportNamedExecutable implements EventPropertyExecutable {


    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }

        List<BreweryLocation> locations = Config.config().events().teleportDestinations().stream().map(BreweryLocation.Supplier::get).toList();
        if (locations.isEmpty()) {
            return ExecutionResult.CONTINUE;
        }
        BreweryLocation teleport = locations.get(RANDOM.nextInt(locations.size()));
        Location location = BukkitAdapter.toLocation(teleport);
        player.teleportAsync(location);
        player.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.TELEPORT_MESSAGE, BukkitMessageUtil.getPlayerTagResolver(player)));
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
