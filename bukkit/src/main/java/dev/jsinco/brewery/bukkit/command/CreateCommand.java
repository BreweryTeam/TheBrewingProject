package dev.jsinco.brewery.bukkit.command;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.recipes.ingredient.Ingredient;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Registry;
import dev.jsinco.brewery.util.moment.Moment;
import dev.jsinco.brewery.util.moment.PassedMoment;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class CreateCommand {

    private static final List<String> TAB_COMPLETIONS = List.of("-a", "--age", "-d", "--distill", "-c", "--cook");
    private static final Map<String, String> REPLACEMENTS = Map.of(
            "-a", "--age",
            "-d", "--distill",
            "-c", "--cook"
    );

    public static boolean onCommand(Player target, CommandSender sender, String[] args) {
        Map<String, BiFunction<Queue<String>, CommandSender, BrewingStep>> operators = Map.of(
                "--age", CreateCommand::getAge,
                "--distill", CreateCommand::getDistill,
                "--cook", CreateCommand::getCook
        );

        Set<String> mandatory = new HashSet<>(Set.of("--cook"));
        Queue<String> arguments = new LinkedList<>(Arrays.asList(args));
        List<BrewingStep> steps = new ArrayList<>();
        while (!arguments.isEmpty()) {
            String operatorName = arguments.poll();
            if (REPLACEMENTS.containsKey(operatorName)) {
                operatorName = REPLACEMENTS.get(operatorName);
            }
            BiFunction<Queue<String>, CommandSender, BrewingStep> operator = operators.get(operatorName);
            if (operator == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_CREATE_UNKNOWN_ARGUMENT, Placeholder.unparsed("argument", operatorName)));
                return false;
            }
            steps.add(operator.apply(arguments, sender));
            mandatory.remove(operatorName);
        }
        if (!mandatory.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_CREATE_MISSING_MANDATORY_ARGUMENT, Placeholder.unparsed("arguments", mandatory.toString())));
            return false;
        }
        ItemStack brewItem = BrewAdapter.toItem(new Brew(steps), new Brew.State.Other());
        target.getWorld().dropItem(target.getLocation(), brewItem);
        sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_CREATE_SUCCESS, Placeholder.component("brew_name", brewItem.effectiveName())));
        return true;
    }

    private static BrewingStep getCook(Queue<String> arguments, CommandSender sender) {
        long cookTime = (long) (Double.parseDouble(arguments.poll()) * Moment.MINUTE);
        CauldronType cauldronType = Registry.CAULDRON_TYPE.get(BreweryKey.parse(arguments.poll()));
        List<String> ingredientStrings = new ArrayList<>();
        while (!arguments.isEmpty() && !arguments.peek().startsWith("-")) {
            ingredientStrings.add(arguments.poll());
        }
        List<String> invalidIngredientArguments = ingredientStrings.stream()
                .filter(ingredient -> !BukkitIngredientManager.INSTANCE.isValidIngredient(ingredient))
                .toList();
        if (!invalidIngredientArguments.isEmpty()) {
            String invalidIngredients = String.join(",", invalidIngredientArguments);
            sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_CREATE_UNKNOWN_ARGUMENT, Placeholder.unparsed("argument", invalidIngredients)));
            throw new IllegalArgumentException("Could not find the ingredient(s): " + invalidIngredients);
        }
        Map<Ingredient, Integer> ingredients = BukkitIngredientManager.INSTANCE.getIngredientsWithAmount(ingredientStrings);
        return new BrewingStep.Cook(new PassedMoment(cookTime), ingredients, cauldronType);
    }

    private static BrewingStep getDistill(Queue<String> arguments, CommandSender sender) {
        int distillAmount = Integer.parseInt(arguments.poll());
        return new BrewingStep.Distill(distillAmount);
    }

    private static BrewingStep getAge(Queue<String> arguments, CommandSender sender) {
        long age = (long) (Double.parseDouble(arguments.poll()) * Moment.AGING_YEAR);
        BarrelType barrelType = Registry.BARREL_TYPE.get(BreweryKey.parse(arguments.poll()));
        return new BrewingStep.Age(new PassedMoment(age), barrelType);
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
