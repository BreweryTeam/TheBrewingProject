package dev.jsinco.brewery.bukkit.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.command.argument.EventArgument;
import dev.jsinco.brewery.bukkit.command.argument.OfflinePlayerArgument;
import dev.jsinco.brewery.bukkit.command.argument.OnlinePlayerArgument;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.event.DrunkEvent;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

public class BreweryCommand {

    private static final SimpleCommandExceptionType ERROR_UNDEFINED_PLAYER = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(MessageUtil.miniMessage(TranslationsConfig.COMMAND_UNDEFINED_PLAYER))
    );

    public static void register(ReloadableRegistrarEvent<Commands> commands) {
        ArgumentBuilder<CommandSourceStack, ?> eventCommand = Commands.argument("event-type", new EventArgument())
                .executes(context -> {
                    Player target = getPlayer(context);
                    DrunkEvent event = context.getArgument("event-type", DrunkEvent.class);
                    TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvent(target.getUniqueId(), event);
                    return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                });
        commands.registrar().register(Commands.literal("tbp")
                .then(CreateCommand.command()
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("brewery.command.create")))
                .then(InfoCommand.command()
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("brewery.command.info")))
                .then(SealCommand.command()
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("brewery.command.seal")))
                .then(StatusCommand.command()
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("brewery.command.status")))
                .then(Commands.literal("reload")
                        .executes(context -> {
                            CommandSender sender = context.getSource().getSender();
                            if (!sender.hasPermission("brewery.command.reload")) {
                                MessageUtil.message(sender, TranslationsConfig.COMMAND_NOT_ENOUGH_PERMISSIONS);
                            } else {
                                TheBrewingProject.getInstance().reload();
                                MessageUtil.message(sender, TranslationsConfig.COMMAND_RELOAD_MESSAGE);
                            }
                            return com.mojang.brigadier.Command.SINGLE_SUCCESS;
                        })
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("brewery.command.reload"))
                )
                .then(Commands.literal("event")
                        .then(eventCommand)
                        .then(playerBranch(argument -> argument.then(eventCommand)))
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("brewery.command.event"))
                ).then(Commands.literal("replicate")
                        .then(playerBranch(argument -> argument.then(ReplicateCommand.command())))
                        .then(ReplicateCommand.command())
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("brewery.command.replicate"))
                )
                .build(), List.of("brew", "brewery"));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> playerBranch(Consumer<ArgumentBuilder<CommandSourceStack, ?>> childAction) {
        ArgumentBuilder<CommandSourceStack, ?> child = Commands.argument("player", new OnlinePlayerArgument());
        childAction.accept(child);
        return Commands.literal("for")
                .then(child)
                .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("brewery.command.other"));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> offlinePlayerBranch(Consumer<ArgumentBuilder<CommandSourceStack, ?>> childAction) {
        ArgumentBuilder<CommandSourceStack, ?> child = Commands.argument("player", new OfflinePlayerArgument());
        childAction.accept(child);
        return Commands.literal("for")
                .then(child)
                .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("brewery.command.other"));
    }


    public static OfflinePlayer getOfflinePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            return context.getArgument("player", OfflinePlayer.class);
        } catch (IllegalArgumentException e) {
            if (context.getSource().getSender() instanceof OfflinePlayer player) {
                return player;
            }
            throw ERROR_UNDEFINED_PLAYER.create();
        }
    }

    public static Player getPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            return context.getArgument("player", Player.class);
        } catch (IllegalArgumentException e) {
            if (context.getSource().getSender() instanceof Player player) {
                return player;
            }
            throw ERROR_UNDEFINED_PLAYER.create();
        }
    }
}
