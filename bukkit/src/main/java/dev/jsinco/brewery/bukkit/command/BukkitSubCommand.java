package dev.jsinco.brewery.bukkit.command;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.command.SubCommand;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface BukkitSubCommand extends SubCommand<TheBrewingProject, CommandSender, OfflinePlayer> {

    @Nullable
    default Player toOnlineTarget(OfflinePlayer target) {
        if (target == null) {
            return null;
        }
        if (target.isOnline()) {
            return target.getPlayer();
        }
        return null;
    }
}
