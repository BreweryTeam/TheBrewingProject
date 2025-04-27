package dev.jsinco.brewery.bukkit.command;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.command.CommandManagerImpl;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BukkitCommandManager implements TabExecutor, dev.jsinco.brewery.command.CommandManager<CommandSender> {

    private final CommandManagerImpl<TheBrewingProject, CommandSender, OfflinePlayer> commandManagerImpl;
    private final TheBrewingProject instance;

    public BukkitCommandManager(TheBrewingProject instance) {
        this.commandManagerImpl = new CommandManagerImpl<>(instance, this);
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull[] args) {
        List<String> argsList = new ArrayList<>(List.of(args));

        OfflinePlayer target = extractTarget(sender, argsList);
        if (target == null && sender instanceof OfflinePlayer player) {
            target = player;
        }

        // Returns a boolean based on if the parent handled this command, should add logic
        // for if the command was not handled by any sub-command
        commandManagerImpl.handle(instance, sender, target, label, argsList);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull[] args) {
        List<String> argsList = new ArrayList<>(List.of(args));

        OfflinePlayer target = extractTarget(sender, argsList);
        if (target == null && sender instanceof OfflinePlayer player) {
            target = player;
        }

        return commandManagerImpl.handleTabComplete(instance, sender, target, label, argsList);
    }

    @Override
    public boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    @Override
    public boolean hasPermission(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }

    // TODO: Refactor and add locale
    private @Nullable OfflinePlayer extractTarget(CommandSender sender, List<String> argsList) {
        int index = argsList.indexOf("for");
        if (index != -1) {
            if (index + 1 >= argsList.size()) {
                sender.sendMessage("Missing player name after 'for'.");
                return null;
            }

            String targetName = argsList.get(index + 1);
            OfflinePlayer target = instance.getServer().getOfflinePlayerIfCached(targetName);
            if (target == null) {
                sender.sendMessage("Unknown player: " + targetName);
                return null;
            }

            // Remove 'for' and the player name from argsList
            argsList.subList(index, Math.min(index + 2, argsList.size())).clear();
            return target;
        }
        return null;
    }
}

