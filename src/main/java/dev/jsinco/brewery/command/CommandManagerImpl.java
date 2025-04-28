package dev.jsinco.brewery.command;

import dev.jsinco.brewery.TheBrewingProjectApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an abstract command manager where T is the main class of this mod or plugin.
 * @param <M> The main class of this mod or plugin.
 * @param <S> the sender type (e.g., CommandSender on Bukkit)
 * @param <T> the target type (e.g., OfflinePlayer on Bukkit)
 */
@AllArgsConstructor
public class CommandManagerImpl<M extends TheBrewingProjectApi, S, T> {

    @Getter
    protected final Map<SubCommandInfo, SubCommand<M, S, T>> subCommands = new HashMap<>();
    protected final M instance;
    private final CommandManager<S> commandManager;

    /**
     * Handles a sub-command.
     * @param instance The instance of the main class of this mod or plugin.
     * @param sender The sender of the command.
     * @param label The label of the command.
     * @param args The arguments of the command.
     * @return true if the command was handled, false otherwise.
     */
    public boolean handle(M instance, S sender, T target, String label, List<String> args) {
        if (args.isEmpty()) {
            // Nothing for us to do up here!
            // Children should handle this in their onCommand method or similar.
            return false;
        }

        String command = args.getFirst();
        for (var mapEntry : this.subCommands.entrySet()) {
            SubCommandInfo info = mapEntry.getKey();
            if (!info.name().equalsIgnoreCase(command)) {
                return false;
            }



            if (info.isPlayerOnly() && !commandManager.isPlayer(sender)) {
                // TODO: needs some kind of message
                return false;
            } else if (!info.permission().isEmpty() && !commandManager.hasPermission(sender, info.permission())) {
                // also needs some kind of message
                return false;
            }
            mapEntry.getValue().execute(instance, sender, target, label, args.subList(1, args.size()));
            return true;
        }

        // Couldn't find a sub-command that matches the command.
        // Leave this up to the children to handle.
        return false;
    }

    /**
     * Handles tab completion for a sub-command.
     * @param instance The instance of the main class of this mod or plugin.
     * @param sender The sender of the command.
     * @param label The label of the command.
     * @param args The arguments of the command.
     * @return a list of completions or null if no completions were found.
     */
    @Nullable
    public List<String> handleTabComplete(M instance, S sender, T target, String label, List<String> args) {
        if (args.isEmpty()) {
            return null;
        }

        List<String> tabCompletions = new ArrayList<>();
        if (commandManager.hasPermission(sender, "brewery.command.other") && !args.contains("for")) {
            tabCompletions.add("for");
        }
        int index = args.indexOf("for");
        if (index != -1) {
            if (index + 1 >= args.size()) {
                return null; // Missing player name after 'for'.
            }
            // Remove 'for' and the player name from argsList
            args.subList(index, Math.min(index + 2, args.size()));
        }

        var mapEntry = this.subCommands.entrySet().stream()
                .filter(it -> it.getKey().name().startsWith(args.getFirst()))
                .filter(it -> commandManager.hasPermission(sender, it.getKey().permission()))
                .findFirst()
                .orElse(null);
        if (mapEntry != null) {
            tabCompletions.addAll(mapEntry.getValue().tabComplete(instance, sender, target, label, args.subList(1, args.size())));

            return tabCompletions;
        }
        return null;
    }

    public boolean addSubCommand(SubCommand<M, S, T> subCommand) {
        SubCommandInfo subCommandInfo = subCommand.commandInfo();
        if (subCommandInfo == null) {
            return false;
        }

        this.subCommands.put(subCommandInfo, subCommand);
        return true;
    }

    public boolean removeSubCommand(SubCommandInfo subCommandInfo) {
        if (!this.subCommands.containsKey(subCommandInfo)) {
            return false;
        }

        this.subCommands.remove(subCommandInfo);
        return true;
    }

    public boolean hasSubCommand(SubCommandInfo subCommandInfo) {
        return this.subCommands.containsKey(subCommandInfo);
    }
}
