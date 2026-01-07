package dev.jsinco.brewery.bukkit.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.command.argument.EnumArgument;
import dev.jsinco.brewery.bukkit.command.argument.OfflinePlayerArgument;
import dev.jsinco.brewery.bukkit.command.argument.OfflinePlayerSelectorArgumentResolver;
import dev.jsinco.brewery.util.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BrewerCommand {

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
                                .executes(context -> execute(context, ADD))
                        )
                        .executes(context -> execute(context, ADD))
                )
        ).then(Commands.literal("remove")
                .then(Commands.argument("brewers", OfflinePlayerArgument.MULTIPLE)
                        .then(Commands.argument("step", IntegerArgumentType.integer(0))
                                .executes(context -> execute(context, REMOVE))
                        )
                        .executes(context -> execute(context, REMOVE))
                )
        ).then(Commands.literal("set")
                .then(Commands.argument("brewers", OfflinePlayerArgument.MULTIPLE)
                        .then(Commands.argument("step", IntegerArgumentType.integer(0))
                                .executes(context -> execute(context, SET))
                        )
                        .executes(context -> execute(context, SET))
                )
        ).then(Commands.literal("clear")
                .then(Commands.argument("step", IntegerArgumentType.integer(0))
                        .executes(context -> execute(context, CLEAR))
                )
                .executes(context -> execute(context, CLEAR))
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context, BrewerAction action) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();
        Slot targetSlot = getTargetSlot(context, BreweryCommand.getPlayer(context));
        ItemStack itemStack = targetSlot.itemGetter.get();
        if (itemStack.isEmpty()) {
            MessageUtil.message(sender, "tbp.command.info.not-a-brew");
            return 1;
        }
        Optional<Integer> stepIndex = getArgument(context, "step", int.class);
        List<OfflinePlayer> brewers = action == CLEAR ? null : context.getArgument("brewers", OfflinePlayerSelectorArgumentResolver.class)
                .resolve(context.getSource());
        BrewAdapter.fromItem(itemStack).ifPresentOrElse(
                brew -> {
                    if (brew.getSteps().isEmpty()) {
                        MessageUtil.message(sender, "tbp.command.brewer.empty");
                        return;
                    }
                    if (stepIndex.isPresent() && stepIndex.get() >= brew.getSteps().size()) {
                        MessageUtil.message(sender, "tbp.command.brewer.step-out-of-bounds");
                        return;
                    }
                    action.modify(sender, brew, stepIndex, brewers)
                            .map(modifiedBrew -> BrewAdapter.toItem(modifiedBrew, new Brew.State.Other()))
                            .ifPresent(targetSlot.itemSetter);
                },
                () -> MessageUtil.message(sender, "tbp.command.info.not-a-brew")
        );
        return 1;
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

    private static <T> Optional<T> getArgument(CommandContext<CommandSourceStack> context, String name, Class<T> resolver) {
        try {
            return Optional.of(context.getArgument(name, resolver));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @FunctionalInterface
    private interface BrewerAction {
        Optional<Brew> modify(CommandSender sender, Brew brew, Optional<Integer> stepIndexArg, List<OfflinePlayer> brewers);
    }

    private static final BrewerAction ADD = (sender, brew, stepIndexArg, players) -> {
        int stepIndex;
        if (stepIndexArg.isPresent()) {
            stepIndex = stepIndexArg.get();
            if (!(brew.getSteps().get(stepIndex) instanceof BrewingStep.AuthoredStep<?>)) {
                MessageUtil.message(sender, "tbp.command.brewer.step-invalid");
                return Optional.empty();
            }
        } else {
            Optional<Integer> resolvedStepIndex = lastAuthoredStepIndex(brew);
            if (resolvedStepIndex.isEmpty()) {
                MessageUtil.message(sender, "tbp.command.brewer.empty");
                return Optional.empty();
            }
            stepIndex = resolvedStepIndex.get();
        }

        sendSuccessMessage("add", sender, players);
        return Optional.of(brew.withModifiedStep(stepIndex, step ->
                ((BrewingStep.AuthoredStep<?>) step).withBrewers(players.stream().map(OfflinePlayer::getUniqueId).toList())
        ));
    };

    private static final BrewerAction REMOVE = (sender, brew, stepIndexArg, players) -> {
        if (stepIndexArg.isPresent()) {
            int stepIndex = stepIndexArg.get();
            if (!(brew.getSteps().get(stepIndex) instanceof BrewingStep.AuthoredStep<?>)) {
                MessageUtil.message(sender, "tbp.command.brewer.step-invalid");
                return Optional.empty();
            }
            sendSuccessMessage("remove", sender, players);
            return Optional.of(brew.withModifiedStep(stepIndex, step -> remove((BrewingStep.AuthoredStep<?>) step, players)));

        } else {
            sendSuccessMessage("remove", sender, players);
            List<BrewingStep> steps = brew.getSteps().stream()
                    .map(step -> {
                        if (step instanceof BrewingStep.AuthoredStep<?> authoredStep) {
                            return remove(authoredStep, players);
                        }
                        return step;
                    })
                    .toList();
            return Optional.of(brew.withStepsReplaced(steps));
        }
    };

    private static BrewingStep remove(BrewingStep.AuthoredStep<?> authoredStep, List<OfflinePlayer> players) {
        return authoredStep.withBrewersReplaced(authoredStep.brewers().stream()
                .filter(uuid -> players.stream().noneMatch(player -> player.getUniqueId().equals(uuid)))
                .toList());
    }

    private static final BrewerAction SET = (sender, brew, stepIndexArg, players) -> {
        List<UUID> brewers = players.stream().map(OfflinePlayer::getUniqueId).toList();
        if (stepIndexArg.isPresent()) {
            int stepIndex = stepIndexArg.get();
            if (!(brew.getSteps().get(stepIndex) instanceof BrewingStep.AuthoredStep<?>)) {
                MessageUtil.message(sender, "tbp.command.brewer.step-invalid");
                return Optional.empty();
            }
            sendSuccessMessage("set", sender, players);
            return Optional.of(brew.withModifiedStep(stepIndex, step ->
                    ((BrewingStep.AuthoredStep<?>) step).withBrewersReplaced(brewers)));

        } else {
            Optional<Integer> resolvedStepIndex = lastAuthoredStepIndex(brew);
            if (resolvedStepIndex.isEmpty()) {
                MessageUtil.message(sender, "tbp.command.brewer.empty");
                return Optional.empty();
            }
            int lastStepIndex = resolvedStepIndex.get();
            sendSuccessMessage("set", sender, players);
            List<BrewingStep> steps = new ArrayList<>();
            for (int i = 0; i < brew.getSteps().size(); i++) {
                BrewingStep step = brew.getSteps().get(i);
                if (i == lastStepIndex) {
                    BrewingStep modifiedStep = ((BrewingStep.AuthoredStep<?>) step).withBrewersReplaced(brewers);
                    steps.add(modifiedStep);
                } else {
                    BrewingStep modifiedStep = step instanceof BrewingStep.AuthoredStep<?> authoredStep ?
                            authoredStep.withBrewersReplaced(List.of()) :
                            step;
                    steps.add(modifiedStep);
                }
            }
            return Optional.of(brew.withStepsReplaced(steps));
        }
    };

    private static final BrewerAction CLEAR = (sender, brew, stepIndexArg, ignored) -> {
        if (stepIndexArg.isPresent()) {
            int stepIndex = stepIndexArg.get();
            if (!(brew.getSteps().get(stepIndex) instanceof BrewingStep.AuthoredStep<?>)) {
                MessageUtil.message(sender, "tbp.command.brewer.step-invalid");
                return Optional.empty();
            }
            sendClearSuccessMessage(sender);
            return Optional.of(brew.withModifiedStep(stepIndex, step ->
                    ((BrewingStep.AuthoredStep<?>) step).withBrewersReplaced(List.of())));

        } else {
            sendClearSuccessMessage(sender);
            List<BrewingStep> steps = brew.getSteps().stream()
                    .map(step -> {
                        if (step instanceof BrewingStep.AuthoredStep<?> authoredStep) {
                            return authoredStep.withBrewersReplaced(List.of());
                        }
                        return step;
                    })
                    .toList();
            return Optional.of(brew.withStepsReplaced(steps));
        }
    };

    private static Optional<Integer> lastAuthoredStepIndex(Brew brew) {
        for (int i = brew.getSteps().size() - 1; i >= 0; i--) {
            BrewingStep step = brew.getSteps().get(i);
            if (step instanceof BrewingStep.AuthoredStep<?>) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private static void sendSuccessMessage(String commandType, CommandSender sender, List<OfflinePlayer> brewers) {
        if (brewers.size() == 1) {
            MessageUtil.message(sender, "tbp.command.brewer." + commandType + "_one",
                    Placeholder.unparsed("brewer", getName(brewers.getFirst())));
        } else {
            MessageUtil.message(sender, "tbp.command.brewer." + commandType + "_many",
                    Placeholder.unparsed("count", String.valueOf(brewers.size())));
        }
    }
    private static void sendClearSuccessMessage(CommandSender sender) {
        MessageUtil.message(sender, "tbp.command.brewer.clear");
    }

    private static String getName(OfflinePlayer player) {
        String name = player.getName();
        if (name != null) {
            return name;
        }
        return player.getUniqueId().toString();
    }

}
