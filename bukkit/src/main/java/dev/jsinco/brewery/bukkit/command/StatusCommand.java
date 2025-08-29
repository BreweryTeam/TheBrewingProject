package dev.jsinco.brewery.bukkit.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.DrunksManager;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.command.argument.FlaggedArgumentBuilder;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StatusCommand {

    private static final DynamicCommandExceptionType MISSING_ARGUMENT = new DynamicCommandExceptionType(value ->
            BukkitMessageUtil.toBrigadier("tbp.command.missing-argument", Placeholder.unparsed("argument_type", value.toString()))
    );

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        OfflinePlayer target = BreweryCommand.getOfflinePlayer(context);
        CommandSender sender = context.getSource().getSender();
        TheBrewingProject.getInstance().getDrunksManager().clear(target.getUniqueId());
        MessageUtil.message(sender, "tbp.command.status.clear.message", Placeholder.unparsed("player_name", target.getName()));
        return 1;
    }

    private static void consumeInternal(CommandContext<CommandSourceStack> context, List<FlaggedArgumentBuilder.Flag> flags, String translationMessage) throws CommandSyntaxException {
        OfflinePlayer target = BreweryCommand.getOfflinePlayer(context);
        DrunksManager drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        if (flags.isEmpty()) {
            throw MISSING_ARGUMENT.create("modifier");
        }
        for (FlaggedArgumentBuilder.Flag flag : flags) {
            drunksManager.consume(target.getUniqueId(), flag.fullName(), context.getArgument(flag.fullName(), Double.class));
        }
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
                Placeholder.component("modifiers", compileModifiersMessage(drunkState)),
                Placeholder.unparsed("player_name", targetName == null ? "null" : targetName),
                Formatter.number("next_event_time", nextEvent == null ? 0 : nextEvent.second() - TheBrewingProject.getInstance().getTime()),
                Placeholder.component("next_event", nextEvent == null ? Component.translatable("tbp.events.nothing-planned") : nextEvent.first().displayName())
        };
    }

    private static Component compileModifiersMessage(DrunkState drunkState) {
        return DrunkenModifierSection.modifiers().drunkenModifiers()
                .stream()
                .map(DrunkenModifier::name)
                .map(modifier -> Component.translatable("tbp.command.status.info.modifier", Argument.tagResolver(
                        Placeholder.unparsed("modifier_name", modifier),
                        Formatter.number("modifier_value", drunkState.modifierValue(modifier))
                )))
                .collect(Component.toComponent(Component.text("\n")));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        Set<FlaggedArgumentBuilder.Flag> flags = DrunkenModifierSection.modifiers().drunkenModifiers()
                .stream()
                .map(drunkenModifier -> new FlaggedArgumentBuilder.Flag(
                                drunkenModifier.name(),
                                null,
                                List.of(new Pair<>("value", DoubleArgumentType.doubleArg(0, 100))),
                                Set.of()
                        )
                )
                .collect(Collectors.toSet());

        ArgumentBuilder<CommandSourceStack, ?> root = Commands.literal("status");

        root.then(BreweryCommand.offlinePlayerBranch(argument -> registerBranches(argument, flags)));
        return root;
    }

    private static void registerBranches(ArgumentBuilder<CommandSourceStack, ?> root, Set<FlaggedArgumentBuilder.Flag> flags) {
        root.then(Commands.literal("info")
                .executes(StatusCommand::info));
        root.then(Commands.literal("clear")
                .executes(StatusCommand::clear));
        ArgumentBuilder<CommandSourceStack, ?> consume = Commands.literal("consume");
        ArgumentBuilder<CommandSourceStack, ?> set = Commands.literal("consume");
        new FlaggedArgumentBuilder(flags, StatusCommand::consume).build().forEach(consume::then);
        new FlaggedArgumentBuilder(flags, StatusCommand::set).build().forEach(set::then);
    }

    private static void set(CommandContext<CommandSourceStack> context, List<FlaggedArgumentBuilder.Flag> flags) throws CommandSyntaxException {
        OfflinePlayer target = BreweryCommand.getOfflinePlayer(context);
        TheBrewingProject.getInstance().getDrunksManager().clear(target.getUniqueId());
        consumeInternal(context, flags, "tbp.command.status.set.message");
    }

    private static void consume(CommandContext<CommandSourceStack> context, List<FlaggedArgumentBuilder.Flag> flags) throws CommandSyntaxException {
        consumeInternal(context, flags, "tbp.command.status.consume.message");
    }
}
