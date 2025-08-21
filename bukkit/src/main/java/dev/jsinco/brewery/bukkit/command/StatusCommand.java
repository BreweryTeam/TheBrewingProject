package dev.jsinco.brewery.bukkit.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.effect.DrunkState;
import dev.jsinco.brewery.effect.DrunksManager;
import dev.jsinco.brewery.event.DrunkEvent;
import dev.jsinco.brewery.util.MessageUtil;
import dev.jsinco.brewery.util.Pair;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class StatusCommand {

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        OfflinePlayer target = BreweryCommand.getOfflinePlayer(context);
        TheBrewingProject.getInstance().getDrunksManager().clear(target.getUniqueId());
        consumeInternal(context, "tbp.command.status.set.message");
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        OfflinePlayer target = BreweryCommand.getOfflinePlayer(context);
        CommandSender sender = context.getSource().getSender();
        TheBrewingProject.getInstance().getDrunksManager().clear(target.getUniqueId());
        MessageUtil.message(sender, "tbp.command.status.clear.message", Placeholder.unparsed("player_name", target.getName()));
        return 1;
    }

    private static int consume(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        consumeInternal(context, "tbp.command.status.consume.message");
        return 1;
    }

    private static void consumeInternal(CommandContext<CommandSourceStack> context, String translationMessage) throws CommandSyntaxException {
        OfflinePlayer target = BreweryCommand.getOfflinePlayer(context);
        int alcohol = context.getArgument("alcohol", int.class);
        int toxins = context.getArgument("toxins", int.class);
        DrunksManager drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        drunksManager.consume(target.getUniqueId(), alcohol, toxins);
        CommandSender sender = context.getSource().getSender();
        MessageUtil.message(sender, translationMessage, compileStatusTagResolvers(target, drunksManager));
    }

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        OfflinePlayer target = BreweryCommand.getOfflinePlayer(context);
        CommandSender sender = context.getSource().getSender();
        DrunksManager drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        MessageUtil.message(sender, "tbp.command.status.info.message", compileStatusTagResolvers(target, drunksManager));
        return 1;
    }

    private static TagResolver[] compileStatusTagResolvers(OfflinePlayer target, DrunksManager drunksManager) {
        DrunkState drunkState = drunksManager.getDrunkState(target.getUniqueId());
        Pair<DrunkEvent, Long> nextEvent = drunksManager.getPlannedEvent(target.getUniqueId());
        drunksManager.getPlannedEvent(target.getUniqueId());
        String targetName = target.getName();
        return new TagResolver[]{
                Formatter.number("alcohol", drunkState == null ? 0 : drunkState.alcohol()),
                Formatter.number("toxins", drunkState == null ? 0 : drunkState.toxins()),
                Placeholder.unparsed("player_name", targetName == null ? "null" : targetName),
                Formatter.number("next_event_time", nextEvent == null ? 0 : nextEvent.second() - TheBrewingProject.getInstance().getTime()),
                Placeholder.component("next_event", nextEvent == null ? Component.translatable("tbp.events.nothing-planned") : nextEvent.first().displayName())
        };
    }

    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        ArgumentBuilder<CommandSourceStack, ?> root = Commands.literal("status");
        List<ArgumentBuilder<CommandSourceStack, ?>> subArguments = List.of(
                Commands.literal("info")
                        .executes(StatusCommand::info),
                Commands.literal("clear")
                        .executes(StatusCommand::clear),
                Commands.literal("consume")
                        .then(Commands.argument("alcohol", IntegerArgumentType.integer(-100, 100))
                                .then(Commands.argument("toxins", IntegerArgumentType.integer(-100, 100))
                                        .executes(StatusCommand::consume)
                                )
                        ),
                Commands.literal("set")
                        .then(Commands.argument("alcohol", IntegerArgumentType.integer(-100, 100))
                                .then(Commands.argument("toxins", IntegerArgumentType.integer(-100, 100))
                                        .executes(StatusCommand::set)
                                )
                        )
        );
        subArguments.forEach(root::then);
        root.then(BreweryCommand.offlinePlayerBranch(argument -> subArguments.forEach(argument::then)));
        return root;
    }
}
