package dev.jsinco.brewery.bukkit.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.DrunksManager;
import dev.jsinco.brewery.api.effect.ModifierConsume;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.command.argument.FlaggedArgumentBuilder;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.format.TimeFormat;
import dev.jsinco.brewery.format.TimeFormatter;
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
import org.jetbrains.annotations.NotNull;

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

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        OfflinePlayer target = BreweryCommand.getOfflinePlayer(context);
        CommandSender sender = context.getSource().getSender();
        DrunksManager drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        MessageUtil.message(sender, "tbp.command.status.info.message", compileStatusTagResolvers(target, drunksManager, List.of()));
        return 1;
    }

    private static TagResolver[] compileStatusTagResolvers(OfflinePlayer target, DrunksManager drunksManager, List<ModifierConsume> consumes) {
        DrunkState drunkState = drunksManager.getDrunkState(target.getUniqueId());
        Pair<DrunkEvent, Long> nextEvent = drunksManager.getPlannedEvent(target.getUniqueId());
        drunksManager.getPlannedEvent(target.getUniqueId());
        String targetName = target.getName();
        if (drunkState == null) {
            drunkState = new DrunkStateImpl(TheBrewingProject.getInstance().getTime(), -1);
        }
        TagResolver consumesResolver = Placeholder.component("consumed_modifiers",
                consumes.stream()
                        .map(consumption ->
                                Component.translatable(
                                        "tbp.command.status.consumed-modifier",
                                        Argument.tagResolver(
                                                Placeholder.unparsed("modifier_name", consumption.modifier().name()),
                                                Formatter.number("modifier_value", consumption.value())
                                        )
                                )
                        ).collect(Component.toComponent(Component.text(",")))
        );
        return new TagResolver[]{
                consumesResolver,
                Placeholder.component("modifiers", compileModifiersMessage(drunkState)),
                Placeholder.unparsed("player_name", targetName == null ? "null" : targetName),
                Placeholder.component("next_event_time", MessageUtil.miniMessage(
                        TimeFormatter.format(nextEvent == null ? 0 : nextEvent.second() - TheBrewingProject.getInstance().getTime(), TimeFormat.CLOCK_MECHANIC))
                ),
                Placeholder.component("next_event", nextEvent == null ? Component.translatable("tbp.events.nothing-planned") : nextEvent.first().displayName())
        };
    }

    private static Component compileModifiersMessage(@NotNull DrunkState drunkState) {
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
        ArgumentBuilder<CommandSourceStack, ?> root = Commands.literal("status");
        registerBranches(root);
        root.then(BreweryCommand.offlinePlayerBranch(StatusCommand::registerBranches));
        return root;
    }

    private static void registerBranches(ArgumentBuilder<CommandSourceStack, ?> root) {
        root.then(Commands.literal("info")
                .executes(StatusCommand::info));
        root.then(Commands.literal("clear")
                .executes(StatusCommand::clear));
        Set<FlaggedArgumentBuilder.Flag> setFlags = DrunkenModifierSection.modifiers().drunkenModifiers()
                .stream()
                .map(drunkenModifier -> new FlaggedArgumentBuilder.Flag(
                                drunkenModifier.name(),
                                null,
                                List.of(new Pair<>(drunkenModifier.name(),
                                        DoubleArgumentType.doubleArg(drunkenModifier.minValue() - drunkenModifier.maxValue(), drunkenModifier.maxValue() - drunkenModifier.minValue()))),
                                Set.of()
                        )
                )
                .collect(Collectors.toSet());
        Set<FlaggedArgumentBuilder.Flag> consumeFlags = DrunkenModifierSection.modifiers().drunkenModifiers()
                .stream()
                .map(drunkenModifier -> new FlaggedArgumentBuilder.Flag(
                                drunkenModifier.name(),
                                null,
                                List.of(new Pair<>(drunkenModifier.name(), DoubleArgumentType.doubleArg(drunkenModifier.minValue(), drunkenModifier.maxValue()))),
                                Set.of()
                        )
                )
                .collect(Collectors.toSet());
        ArgumentBuilder<CommandSourceStack, ?> consume = Commands.literal("consume");
        ArgumentBuilder<CommandSourceStack, ?> set = Commands.literal("set");
        new FlaggedArgumentBuilder(consumeFlags, StatusCommand::consume).build().forEach(consume::then);
        new FlaggedArgumentBuilder(setFlags, StatusCommand::set).build().forEach(set::then);
        root.then(consume);
        root.then(set);
    }

    private static void set(CommandContext<CommandSourceStack> context, List<FlaggedArgumentBuilder.Flag> flags) throws CommandSyntaxException {
        OfflinePlayer target = BreweryCommand.getOfflinePlayer(context);
        if (flags.isEmpty()) {
            throw MISSING_ARGUMENT.create("modifier");
        }
        DrunksManager drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        DrunkState drunkState = drunksManager.getDrunkState(target.getUniqueId());
        if (drunkState == null) {
            drunkState = new DrunkStateImpl(TheBrewingProject.getInstance().getTime(), -1);
        }
        final DrunkState finalDrunkState = drunkState;
        List<ModifierConsume> consumes = flags.stream()
                .map(flag -> {
                            DrunkenModifier modifier = DrunkenModifierSection.modifiers().modifier(flag.fullName());
                            return new ModifierConsume(
                                    modifier,
                                    context.getArgument(flag.fullName(), Double.class) - finalDrunkState.modifierValue(modifier)
                            );
                        }
                )
                .toList();
        drunksManager.consume(target.getUniqueId(), consumes);
        CommandSender sender = context.getSource().getSender();
        MessageUtil.message(sender, "tbp.command.status.set.message", compileStatusTagResolvers(target, drunksManager, consumes));
    }

    private static void consume(CommandContext<CommandSourceStack> context, List<FlaggedArgumentBuilder.Flag> flags) throws CommandSyntaxException {
        OfflinePlayer target = BreweryCommand.getOfflinePlayer(context);
        DrunksManager drunksManager = TheBrewingProject.getInstance().getDrunksManager();
        if (flags.isEmpty()) {
            throw MISSING_ARGUMENT.create("modifier");
        }
        List<ModifierConsume> consumes = flags.stream()
                .map(flag -> new ModifierConsume(
                        DrunkenModifierSection.modifiers().modifier(flag.fullName()), context.getArgument(flag.fullName(), Double.class)
                ))
                .toList();
        drunksManager.consume(target.getUniqueId(), consumes);
        CommandSender sender = context.getSource().getSender();
        MessageUtil.message(sender, "tbp.command.status.consume.message", compileStatusTagResolvers(target, drunksManager, consumes));
    }
}
