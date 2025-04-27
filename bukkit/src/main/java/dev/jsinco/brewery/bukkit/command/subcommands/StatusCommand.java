package dev.jsinco.brewery.bukkit.command.subcommands;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.command.BukkitCommandManager;
import dev.jsinco.brewery.bukkit.command.BukkitSubCommand;
import dev.jsinco.brewery.command.SubCommandInfo;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.effect.DrunksManagerImpl;
import dev.jsinco.brewery.event.DrunkEvent;
import dev.jsinco.brewery.util.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

@SubCommandInfo(
        name = "status",
        permission = "brewery.command.status"
)
public class StatusCommand implements BukkitSubCommand {
    @Override
    public void execute(TheBrewingProject instance, CommandSender sender, OfflinePlayer player, String label, List<String> args) {
        DrunksManagerImpl<?> drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        switch (args.getFirst()) {
            case "info" -> StatusCommand.info(player, sender, drunksManager, args.subList(1, args.size()));
            case "consume" ->
                    StatusCommand.consume(player, sender, drunksManager, args.subList(1, args.size()));
            case "clear" ->
                    StatusCommand.clear(player, sender, drunksManager, args.subList(1, args.size()));
            case "set" -> StatusCommand.set(player, sender, drunksManager, args.subList(1, args.size()));
            default -> {}
        };
    }

    private static boolean set(OfflinePlayer target, CommandSender sender, DrunksManagerImpl<?> drunksManager, @NotNull List<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_MISSING_ARGUMENT, Placeholder.unparsed("argument_type", "<alcohol>")));
            return true;
        }
        drunksManager.clear(target.getUniqueId());
        return consume(target, sender, drunksManager, args);
    }

    private static boolean clear(@NotNull OfflinePlayer target, CommandSender sender, DrunksManagerImpl<?> drunksManager, @NotNull List<String> args) {
        drunksManager.clear(target.getUniqueId());
        sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_STATUS_CLEAR_MESSAGE, Placeholder.unparsed("player_name", target.getName())));
        return true;
    }

    private static boolean consume(OfflinePlayer target, CommandSender sender, DrunksManagerImpl<?> drunksManager, @NotNull List<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_MISSING_ARGUMENT, Placeholder.unparsed("argument_type", "<alcohol>")));
            return true;
        }
        drunksManager.clear(target.getUniqueId());
        int alcohol = Integer.parseInt(args.getFirst());
        int toxins;
        if (args.size() == 2) {
            toxins = Integer.parseInt(args.get(1));
        } else {
            toxins = 0;
        }
        drunksManager.consume(target.getUniqueId(), alcohol, toxins);
        sender.sendMessage(compileStatusMessage(target, drunksManager, TranslationsConfig.COMMAND_STATUS_SET_MESSAGE));
        return true;
    }

    private static boolean info(OfflinePlayer target, CommandSender sender, DrunksManagerImpl<?> drunksManager, @NotNull List<String> args) {
        if (target == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_UNKNOWN_PLAYER, Placeholder.unparsed("player_name", args.getFirst())));
            return true;
        }
        sender.sendMessage(compileStatusMessage(target, drunksManager, TranslationsConfig.COMMAND_STATUS_INFO_MESSAGE));
        return true;
    }

    private static Component compileStatusMessage(OfflinePlayer target, DrunksManagerImpl<?> drunksManager, String message) {
        DrunkStateImpl drunkState = drunksManager.getDrunkState(target.getUniqueId());
        Pair<DrunkEvent, Long> nextEvent = drunksManager.getPlannedEvent(target.getUniqueId());
        drunksManager.getPlannedEvent(target.getUniqueId());
        String targetName = target.getName();
        return MiniMessage.miniMessage().deserialize(
                message,
                Formatter.number("alcohol", drunkState == null ? 0 : drunkState.alcohol()),
                Formatter.number("toxins", drunkState == null ? 0 : drunkState.toxins()),
                Placeholder.unparsed("player_name", targetName == null ? "null" : targetName),
                Formatter.number("next_event_time", nextEvent == null ? 0 : nextEvent.second() - TheBrewingProject.getInstance().getTime()),
                Placeholder.unparsed("next_event", nextEvent == null ? TranslationsConfig.NO_EVENT_PLANNED : nextEvent.first().displayName())
        );
    }

    @Override
    public List<String> tabComplete(TheBrewingProject instance, CommandSender sender, OfflinePlayer player, String label, List<String> args) {
        if (args.size() == 1) {
            return Stream.of("info", "consume", "set", "clear")
                    .toList();
        }
        return switch (args.getFirst()) {
            case "consume", "set" -> {
                if (args.size() == 2) {
                    yield BukkitCommandManager.INTEGER_TAB_COMPLETIONS;
                } else if (args.size() == 3) {
                    yield BukkitCommandManager.INTEGER_TAB_COMPLETIONS;
                }
                yield List.of();
            }
            default -> List.of();
        };
    }
}
