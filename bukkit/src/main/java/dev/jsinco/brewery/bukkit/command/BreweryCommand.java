package dev.jsinco.brewery.bukkit.command;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.effect.event.NamedDrunkEventExecutor;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.event.CustomEvent;
import dev.jsinco.brewery.event.DrunkEvent;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Registry;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class BreweryCommand implements TabExecutor {

    public static final List<String> INTEGER_TAB_COMPLETIONS = compileIntegerTabCompletions();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }
        SubCommand subCommand;
        try {
            subCommand = SubCommand.valueOf(args[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_ILLEGAL_ARGUMENT_DETAILED, Placeholder.unparsed("argument", args[0])));
            return true;
        }
        if (!sender.hasPermission(subCommand.getPermissionNode())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_NOT_ENOUGH_PERMISSIONS));
            return true;
        }
        OfflinePlayer target;
        if (args.length > 1 && args[1].equals("for")) {
            if (!sender.hasPermission("brewery.command.other")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_NOT_ENOUGH_PERMISSIONS));
                return true;
            }
            if (args.length > 2) {
                target = Bukkit.getOfflinePlayerIfCached(args[2]);
                if (target == null) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_UNKNOWN_PLAYER, Placeholder.unparsed("player_name", args[2])));
                    return true;
                }
            } else {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_MISSING_ARGUMENT, Placeholder.unparsed("argument_type", "<player_name>")));
                return true;
            }
            args = Arrays.copyOfRange(args, 2, args.length);
        } else if (sender instanceof OfflinePlayer player) {
            target = player;
        } else {
            target = null;
        }
        if ((subCommand.isRequiresOfflinePlayer() && target == null) || (!(target instanceof Player) && subCommand.isRequiresPlayer())) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_UNDEFINED_PLAYER));
            return true;
        }
        return switch (subCommand) {
            case CREATE -> CreateCommand.onCommand((Player) target, sender, Arrays.copyOfRange(args, 1, args.length));
            case EVENT -> {
                if(args.length < 2) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_MISSING_ARGUMENT, Placeholder.unparsed("argument_type", "<event_type>")));
                    yield true;
                }
                NamedDrunkEvent namedDrunkEvent = Registry.DRUNK_EVENT.get(BreweryKey.parse(args[1]));
                if (namedDrunkEvent != null) {
                    NamedDrunkEventExecutor.doDrunkEvent(target.getUniqueId(), namedDrunkEvent);
                    yield true;
                }
                CustomEvent customEvent = TheBrewingProject.getInstance().getCustomDrunkEventRegistry().getCustomEvent(BreweryKey.parse(args[1]));
                if (customEvent == null) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_ILLEGAL_ARGUMENT_DETAILED, Placeholder.unparsed("argument", args[1])));
                    yield true;
                }
                TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvent(target.getUniqueId(), customEvent);
                yield true;
            }
            case STATUS -> StatusCommand.onCommand(target, sender, Arrays.copyOfRange(args, 1, args.length));
            case INFO -> InfoCommand.onCommand((Player) target, sender, Arrays.copyOfRange(args, 1, args.length));
            case RELOAD -> {
                TheBrewingProject.getInstance().reload();
                sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_RELOAD_MESSAGE));
                yield true;
            }
            case SEAL -> SealCommand.onCommand((Player) target, sender, Arrays.copyOfRange(args, 1, args.length));
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return List.of();
        }
        if (args.length == 1) {
            final String[] finalArgs = args;
            return Arrays.stream(SubCommand.values())
                    .filter(subCommand -> sender.hasPermission(subCommand.getPermissionNode()))
                    .map(SubCommand::name)
                    .map(subCommand -> subCommand.toLowerCase(Locale.ROOT))
                    .filter(subCommand -> subCommand.startsWith(finalArgs[0]))
                    .toList();
        }
        SubCommand subCommand;
        try {
            subCommand = SubCommand.valueOf(args[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return List.of();
        }
        if (!sender.hasPermission(subCommand.getPermissionNode())) {
            return List.of();
        }
        List<String> tabCompletions = new ArrayList<>();
        if (subCommand.isRequiresOfflinePlayer() && sender.hasPermission("brewery.command.other")) {
            if (args.length == 2) {
                tabCompletions.add("for");
            }
            if (args[1].equals("for")) {
                if (args.length == 3) {
                    return null;
                } else if (args.length > 3) {
                    args = Arrays.copyOfRange(args, 2, args.length);
                }
            }
        }
        tabCompletions.addAll(switch (subCommand) {
            case CREATE -> CreateCommand.tabComplete(Arrays.copyOfRange(args, 1, args.length));
            case EVENT -> {
                if (args.length > 2) {
                    yield List.of();
                }
                yield Stream.concat(Arrays.stream(NamedDrunkEvent.values()), TheBrewingProject.getInstance().getCustomDrunkEventRegistry().events().stream())
                        .map(DrunkEvent::key)
                        .map(BreweryKey::key)
                        .toList();
            }
            case INFO -> INTEGER_TAB_COMPLETIONS;
            case SEAL -> {
                if (args.length == 2) {
                    yield List.of("<volume-info>", "all");
                }
                yield List.of();
            }
            case STATUS -> StatusCommand.tabComplete(Arrays.copyOfRange(args, 1, args.length));
            case RELOAD -> List.of();
        });
        final String[] finalArgs = args;
        return tabCompletions.
                stream()
                .filter(string -> string.startsWith(finalArgs[finalArgs.length - 1]))
                .toList();
    }

    private static List<String> compileIntegerTabCompletions() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            strings.add(String.valueOf(i));
        }
        return List.copyOf(strings);
    }
}
