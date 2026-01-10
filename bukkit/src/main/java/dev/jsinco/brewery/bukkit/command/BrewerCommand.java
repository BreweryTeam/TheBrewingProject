package dev.jsinco.brewery.bukkit.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.command.argument.EnumArgument;
import dev.jsinco.brewery.bukkit.command.argument.OfflinePlayerArgument;
import dev.jsinco.brewery.bukkit.command.argument.OfflinePlayerSelectorArgumentResolver;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BrewerCommand {

    private static DynamicCommandExceptionType INDEX_OUT_OF_BOUNDS = new DynamicCommandExceptionType(object ->
            BukkitMessageUtil.toBrigadier("tbp.command.brewer.step-out-of-bounds", Formatter.number("max_index", (Number) object))
    );

    private static final int PLAYER_INVENTORY_SIZE = 41;

    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        ArgumentBuilder<CommandSourceStack, ?> withIndex = Commands.argument("inventory_slot",
                IntegerArgumentType.integer(0, PLAYER_INVENTORY_SIZE - 1));
        appendBrewerModification(withIndex);
        ArgumentBuilder<CommandSourceStack, ?> withNamedSlot = Commands.argument("equipment_slot",
                new EnumArgument<>(EquipmentSlot.class));
        appendBrewerModification(withNamedSlot);

        ArgumentBuilder<CommandSourceStack, ?> command = Commands.literal("brewer");
        command.then(withNamedSlot); // /tbp brewer <inventory_slot> ...
        command.then(withIndex); // /tbp brewer <equipment_slot> ...
        command.then(BreweryCommand.playerBranch(argument -> {
            argument.then(withNamedSlot); // /tbp brewer for <player> <inventory_slot> ...
            argument.then(withIndex); // /tbp brewer for <player> <equipment_slot> ...
            appendBrewerModification(argument); // /tbp brewer for <player> ...
        }));
        appendBrewerModification(command); // /tbp brewer ...
        return command;
    }

    private static void appendBrewerModification(ArgumentBuilder<CommandSourceStack, ?> builder) {
        builder.then(Commands.literal("add")
                .then(Commands.argument("brewers", OfflinePlayerArgument.MULTIPLE)
                        .then(Commands.argument("step", IntegerArgumentType.integer(0))
                                .executes(context -> modifyBrew(context,
                                        BrewerCommand::addBrewers,
                                        BrewerCommand::addBrewersToLast,
                                        "tbp.command.brewer.add"
                                ))
                        )
                        .executes(context -> modifyBrew(context,
                                BrewerCommand::addBrewers,
                                BrewerCommand::addBrewersToLast,
                                "tbp.command.brewer.add"
                        ))
                )
        ).then(Commands.literal("remove")
                .then(Commands.argument("brewers", OfflinePlayerArgument.MULTIPLE)
                        .then(Commands.argument("step", IntegerArgumentType.integer(0))
                                .executes(context -> modifyBrew(context,
                                        BrewerCommand::removeBrewers,
                                        "tbp.command.brewer.remove"
                                ))
                        )
                        .executes(context -> modifyBrew(context,
                                BrewerCommand::removeBrewers,
                                "tbp.command.brewer.remove"
                        ))
                )
        ).then(Commands.literal("set")
                .then(Commands.argument("brewers", OfflinePlayerArgument.MULTIPLE)
                        .then(Commands.argument("step", IntegerArgumentType.integer(0))
                                .executes(context -> modifyBrew(context,
                                        BrewerCommand::setBrewers,
                                        BrewerCommand::setBrewersClearingOtherSteps,
                                        "tbp.command.brewer.set"
                                ))
                        )
                        .executes(context -> modifyBrew(context,
                                BrewerCommand::setBrewers,
                                BrewerCommand::setBrewersClearingOtherSteps,
                                "tbp.command.brewer.set"
                        ))
                )
        ).then(Commands.literal("clear")
                .then(Commands.argument("step", IntegerArgumentType.integer(0))
                        .executes(context -> modifyBrew(context,
                                BrewerCommand::clearBrewers,
                                "tbp.command.brewer.clear"
                        ))
                )
                .executes(context -> modifyBrew(context,
                        BrewerCommand::clearBrewers,
                        "tbp.command.brewer.clear"
                ))
        );
    }

    private static BrewingStep addBrewers(BrewingStep step, List<UUID> brewers) {
        if (step instanceof BrewingStep.AuthoredStep<?> authoredStep) {
            return authoredStep.withBrewers(brewers);
        }
        return step;
    }
    private static Brew addBrewersToLast(Brew brew, List<UUID> brewers) {
        return lastAuthoredStepIndex(brew).map(index ->
                brew.withModifiedStep(index, ignored -> addBrewers(brew.getSteps().get(index), brewers))
        ).orElse(brew);
    }

    private static BrewingStep removeBrewers(BrewingStep step, List<UUID> brewers) {
        if (step instanceof BrewingStep.AuthoredStep<?> authoredStep) {
            return authoredStep.withBrewersReplaced(
                    authoredStep.brewers().stream()
                            .filter(uuid -> brewers.stream().noneMatch(uuid::equals))
                            .toList()
            );
        }
        return step;
    }

    private static BrewingStep setBrewers(BrewingStep step, List<UUID> brewers) {
        if (step instanceof BrewingStep.AuthoredStep<?> authoredStep) {
            return authoredStep.withBrewersReplaced(brewers);
        }
        return step;
    }
    private static Brew setBrewersClearingOtherSteps(Brew brew, List<UUID> brewers) {
        Brew cleared = modifyAllBrewSteps(brew, BrewerCommand::clearBrewers, brewers);
        return lastAuthoredStepIndex(cleared).map(index ->
                brew.withModifiedStep(index, ignored -> addBrewers(cleared.getSteps().get(index), brewers))
        ).orElse(brew);
    }

    private static BrewingStep clearBrewers(BrewingStep step, List<UUID> ignored) {
        if (step instanceof BrewingStep.AuthoredStep<?> authoredStep) {
            return authoredStep.withBrewersReplaced(List.of());
        }
        return step;
    }

    private static int modifyBrew(CommandContext<CommandSourceStack> context,
                                  BiFunction<BrewingStep, List<UUID>, BrewingStep> stepOperation,
                                  String successMessageKey) throws CommandSyntaxException {
        return modifyBrew(
                context,
                stepOperation,
                (brew, brewers) -> modifyAllBrewSteps(brew, stepOperation, brewers),
                successMessageKey
        );
    }
    private static int modifyBrew(CommandContext<CommandSourceStack> context,
                                  BiFunction<BrewingStep, List<UUID>, BrewingStep> stepOperation,
                                  BiFunction<Brew, List<UUID>, Brew> brewOperation,
                                  String successMessageKey) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        Slot targetSlot = getTargetSlot(context, BreweryCommand.getPlayer(context));
        ItemStack itemStack = targetSlot.itemGetter.get();
        if (itemStack.isEmpty()) {
            MessageUtil.message(sender, "tbp.command.info.not-a-brew");
            return 1;
        }
        Optional<Integer> stepIndex = getArgument(context, "step", int.class);
        List<OfflinePlayer> brewers = getBrewers(context);
        List<UUID> brewerUuids = brewers
                .stream()
                .map(OfflinePlayer::getUniqueId)
                .toList();
        Optional<Brew> brewOptional = BrewAdapter.fromItem(itemStack);
        if (stepIndex.isPresent() && brewOptional.isPresent() && brewOptional.get().stepAmount() <= stepIndex.get()) {
            throw INDEX_OUT_OF_BOUNDS.create(brewOptional.get().stepAmount() - 1);
        }
        if(brewOptional.map(Brew::stepAmount).filter(stepAmount -> stepAmount <= 0).isPresent()){
            MessageUtil.message(sender, "tbp.command.brewer.empty");
            return 1;
        }
        brewOptional
                .map(brew -> stepIndex.map(index ->
                                brew.withModifiedStep(index, brewingStep -> stepOperation.apply(brewingStep, brewerUuids))
                        ).orElseGet(() -> brewOperation.apply(brew, brewerUuids))
                ).ifPresentOrElse(brew -> {
                    targetSlot.itemSetter().accept(BrewAdapter.toItem(brew, new Brew.State.Other()));
                    MessageUtil.message(sender, successMessageKey, Placeholder.component("brewers",
                            brewers.stream()
                                    .map(BrewerCommand::getName)
                                    .map(Component::text)
                                    .collect(Component.toComponent(Component.text(", ")))
                    ));
                }, () -> MessageUtil.message(sender, "tbp.command.info.not-a-brew"));
        return 1;
    }

    private static Brew modifyAllBrewSteps(Brew brew, BiFunction<BrewingStep, List<UUID>, BrewingStep> operation, List<UUID> brewers) {
        return new BrewImpl(
                brew.getSteps().stream()
                        .map(brewingStep -> operation.apply(brewingStep, brewers))
                        .toList()
        );
    }

    private static Optional<Integer> lastAuthoredStepIndex(Brew brew) {
        for (int i = brew.getSteps().size() - 1; i >= 0; i--) {
            BrewingStep step = brew.getSteps().get(i);
            if (step instanceof BrewingStep.AuthoredStep<?>) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private static Slot getTargetSlot(CommandContext<CommandSourceStack> context, Player targetPlayer) {
        PlayerInventory inventory = targetPlayer.getInventory();
        return getArgument(context, "equipment_slot", EquipmentSlot.class).map(equipmentSlot -> new Slot(
                        () -> inventory.getItem(equipmentSlot),
                        itemStack -> inventory.setItem(equipmentSlot, itemStack)
                ))
                .or(() -> getArgument(context, "inventory_slot", int.class).map(inventorySlot -> new Slot(
                        () -> inventory.getItem(inventorySlot),
                        itemStack -> inventory.setItem(inventorySlot, itemStack)
                )))
                .orElseGet(() -> new Slot(
                        inventory::getItemInMainHand,
                        inventory::setItemInMainHand
                ));
    }

    private record Slot(Supplier<ItemStack> itemGetter, Consumer<ItemStack> itemSetter) {
    }

    private static List<OfflinePlayer> getBrewers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            return context.getArgument("brewers", OfflinePlayerSelectorArgumentResolver.class)
                    .resolve(context.getSource());
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    private static <T> Optional<T> getArgument(CommandContext<CommandSourceStack> context, String name, Class<T> resolver) {
        try {
            return Optional.of(context.getArgument(name, resolver));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static String getName(OfflinePlayer player) {
        String name = player.getName();
        if (name != null) {
            return name;
        }
        return player.getUniqueId().toString();
    }

}
