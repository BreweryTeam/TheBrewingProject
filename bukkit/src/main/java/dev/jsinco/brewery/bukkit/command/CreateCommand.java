package dev.jsinco.brewery.bukkit.command;

import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.moment.PassedMoment;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Registry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class CreateCommand {

    private static final List<String> TAB_COMPLETIONS = List.of("-a", "--age", "-d", "--distill", "-c", "--cook", "--mix", "-m");
    private static final Map<String, String> REPLACEMENTS = Map.of(
            "-a", "--age",
            "-d", "--distill",
            "-c", "--cook",
            "-m", "--mix"
    );

    public static boolean onCommand(Player target, CommandSender sender, String[] args) {
        Map<String, BiFunction<Queue<String>, CommandSender, @Nullable BrewingStep>> operators = Map.of(
                "--age", CreateCommand::getAge,
                "--distill", CreateCommand::getDistill,
                "--cook", CreateCommand::getCook,
                "--mix", CreateCommand::getMix
        );

        Queue<String> arguments = new LinkedList<>(Arrays.asList(args));
        if(arguments.isEmpty()) {
            sender.sendMessage(missingArgument("brewing_step"));
            return true;
        }
        List<BrewingStep> steps = new ArrayList<>();
        while (!arguments.isEmpty()) {
            String operatorName = arguments.poll();
            if (REPLACEMENTS.containsKey(operatorName)) {
                operatorName = REPLACEMENTS.get(operatorName);
            }
            BiFunction<Queue<String>, CommandSender, BrewingStep> operator = operators.get(operatorName);
            if (operator == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_ILLEGAL_ARGUMENT_DETAILED, Placeholder.unparsed("argument", operatorName)));
                return true;
            }
            BrewingStep brewingStep = operator.apply(arguments, sender);
            if (brewingStep == null) {
                return true;
            }
            steps.add(brewingStep);
        }
        ItemStack brewItem = BrewAdapter.toItem(new BrewImpl(steps), new BrewImpl.State.Other());
        target.getWorld().dropItem(target.getLocation(), brewItem);
        ItemMeta brewItemMeta = brewItem.getItemMeta();
        sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_CREATE_SUCCESS, Placeholder.component("brew_name", brewItemMeta.hasCustomName() ? brewItemMeta.customName() : brewItemMeta.itemName())));
        return true;
    }

    private static BrewingStep getMix(Queue<String> arguments, CommandSender sender) {
        if (arguments.size() < 2) {
            sender.sendMessage(missingArgument(new String[]{"cook_time", "ingredients"}[arguments.size()]));
            return null;
        }
        Double cookTime = parseDouble(arguments.poll(), sender);
        if (cookTime == null) {
            return null;
        }
        Map<Ingredient, Integer> ingredients = retrieveIngredients(arguments, sender);
        if (ingredients == null) {
            return null;
        }
        return new BrewingStep.Mix(new PassedMoment((long) (cookTime * Moment.MINUTE)), ingredients);
    }

    private static @Nullable Map<Ingredient, Integer> retrieveIngredients(Queue<String> arguments, CommandSender sender) {
        List<String> ingredientStrings = new ArrayList<>();
        while (!arguments.isEmpty() && !arguments.peek().startsWith("-")) {
            ingredientStrings.add(arguments.poll());
        }
        List<String> invalidIngredientArguments = ingredientStrings.stream()
                .filter(ingredient -> !BukkitIngredientManager.INSTANCE.isValidIngredient(ingredient))
                .toList();
        if (!invalidIngredientArguments.isEmpty()) {
            String invalidIngredients = String.join(",", invalidIngredientArguments);
            sender.sendMessage(illegalArgument(invalidIngredients));
            return null;
        }
        return BukkitIngredientManager.INSTANCE.getIngredientsWithAmount(ingredientStrings);
    }

    private static @Nullable BrewingStep getCook(Queue<String> arguments, CommandSender sender) {
        if (arguments.size() < 3) {
            sender.sendMessage(missingArgument(new String[]{"cook_time", "cauldron_type", "ingredients"}[arguments.size()]));
            return null;
        }
        Double cookTime = parseDouble(arguments.poll(), sender);
        if (cookTime == null) {
            return null;
        }
        String cauldronString = arguments.poll();
        CauldronType cauldronType = Registry.CAULDRON_TYPE.get(BreweryKey.parse(cauldronString));
        if (cauldronType == null) {
            sender.sendMessage(illegalArgument(cauldronString));
            return null;
        }
        Map<Ingredient, Integer> ingredients = retrieveIngredients(arguments, sender);
        if (ingredients == null) {
            return null;
        }
        return new BrewingStep.Cook(new PassedMoment((long) (cookTime * Moment.MINUTE)), ingredients, cauldronType);
    }

    private static BrewingStep getDistill(Queue<String> arguments, CommandSender sender) {
        if (arguments.isEmpty()) {
            sender.sendMessage(missingArgument("runs"));
            return null;
        }
        String distillString = arguments.poll();
        try {
            int distillAmount = Integer.parseInt(distillString);
            return new BrewingStep.Distill(distillAmount);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(illegalArgument(distillString));
            return null;
        }
    }

    private static BrewingStep getAge(Queue<String> arguments, CommandSender sender) {
        if (arguments.size() < 2) {
            sender.sendMessage(missingArgument(new String[]{"aging_years", "barrel_type"}[arguments.size()]));
            return null;
        }
        Double agingYear = parseDouble(arguments.poll(), sender);
        if (agingYear == null) {
            return null;
        }
        String barrelTypeString = arguments.poll();
        BarrelType barrelType = Registry.BARREL_TYPE.get(BreweryKey.parse(barrelTypeString));
        if (barrelType == null) {
            sender.sendMessage(illegalArgument(barrelTypeString));
            return null;
        }
        return new BrewingStep.Age(new PassedMoment((long) (agingYear * Moment.AGING_YEAR)), barrelType);
    }

    private static Component illegalArgument(String argument) {
        return MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_ILLEGAL_ARGUMENT_DETAILED, Placeholder.unparsed("argument", argument));
    }

    private static Component missingArgument(String argumentType) {
        return MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_MISSING_ARGUMENT, Placeholder.unparsed("argument_type", "<" + argumentType + ">"));
    }

    private static @Nullable Double parseDouble(String argument, CommandSender sender) {
        try {
            return Double.parseDouble(argument);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(illegalArgument(argument));
            return null;
        }
    }

    public static List<String> tabComplete(@NotNull String @NotNull [] args) {
        for (int i = args.length - 2; i >= 0; i--) {
            if (TAB_COMPLETIONS.contains(args[i])) {
                int precedingArgsLength = args.length - i - 1;
                return switch (REPLACEMENTS.getOrDefault(args[i], args[i])) {
                    case "--age" -> {
                        if (precedingArgsLength == 1) {
                            yield BreweryCommand.INTEGER_TAB_COMPLETIONS;
                        } else if (precedingArgsLength == 2) {
                            yield Registry.BARREL_TYPE.values().stream()
                                    .map(BarrelType::key)
                                    .map(BreweryKey::key)
                                    .toList();
                        } else if (precedingArgsLength == 3) {
                            yield TAB_COMPLETIONS;
                        }
                        yield List.of();
                    }
                    case "--cook" -> {
                        if (precedingArgsLength == 1) {
                            yield BreweryCommand.INTEGER_TAB_COMPLETIONS;
                        } else if (precedingArgsLength == 2) {
                            yield Registry.CAULDRON_TYPE.values().stream()
                                    .map(CauldronType::key)
                                    .map(BreweryKey::key)
                                    .toList();
                        }
                        yield Stream.concat(Stream.of("<ingredient/amount>"), TAB_COMPLETIONS.stream()).toList();
                    }
                    case "--distill" -> {
                        if (precedingArgsLength == 1) {
                            yield BreweryCommand.INTEGER_TAB_COMPLETIONS;
                        } else if (precedingArgsLength == 2) {
                            yield TAB_COMPLETIONS;
                        }
                        yield List.of();
                    }
                    case "--mix" -> {
                        if (precedingArgsLength == 1) {
                            yield BreweryCommand.INTEGER_TAB_COMPLETIONS;
                        }
                        yield Stream.concat(Stream.of("<ingredient/amount>"), TAB_COMPLETIONS.stream()).toList();
                    }
                    default ->
                            throw new IllegalStateException("Unexpected value: " + REPLACEMENTS.getOrDefault(args[i], args[i]));
                };
            } else if (args[i].startsWith("-")) {
                return List.of();
            }
        }
        return TAB_COMPLETIONS;
    }
}
