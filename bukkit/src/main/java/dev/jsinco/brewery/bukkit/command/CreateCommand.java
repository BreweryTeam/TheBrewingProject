package dev.jsinco.brewery.bukkit.command;

import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.command.SubCommandInfo;
import dev.jsinco.brewery.configuration.locale.TranslationsConfig;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Registry;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.moment.PassedMoment;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@SubCommandInfo(
        name = "create",
        permission = "brewery.command.create"
)
public class CreateCommand implements BukkitSubCommand {

    private static final List<String> TAB_COMPLETIONS = List.of("-a", "--age", "-d", "--distill", "-c", "--cook", "--mix", "-m");
    private static final Map<String, String> REPLACEMENTS = Map.of(
            "-a", "--age",
            "-d", "--distill",
            "-c", "--cook",
            "-m", "--mix"
    );

    @Override
    public void execute(TheBrewingProject instance, CommandSender sender, OfflinePlayer offlineTarget, String label, List<String> args) {
        Player target = toOnlineTarget(offlineTarget);
        if (target == null) {
            //sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_CREATE_TARGET_OFFLINE));
            return;
        }

        Map<String, BiFunction<Queue<String>, CommandSender, BrewingStep>> operators = Map.of(
                "--age", this::getAge,
                "--distill", this::getDistill,
                "--cook", this::getCook,
                "--mix", this::getMix
        );

        Queue<String> arguments = new LinkedList<>(args);
        List<BrewingStep> steps = new ArrayList<>();
        while (!arguments.isEmpty()) {
            String operatorName = arguments.poll();
            if (REPLACEMENTS.containsKey(operatorName)) {
                operatorName = REPLACEMENTS.get(operatorName);
            }
            BiFunction<Queue<String>, CommandSender, BrewingStep> operator = operators.get(operatorName);
            if (operator == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_CREATE_UNKNOWN_ARGUMENT, Placeholder.unparsed("argument", operatorName)));
                return;
            }
            steps.add(operator.apply(arguments, sender));
        }
        ItemStack brewItem = BrewAdapter.toItem(new BrewImpl(steps), new BrewImpl.State.Other());
        target.getWorld().dropItem(target.getLocation(), brewItem);
        sender.sendMessage(MiniMessage.miniMessage().deserialize(TranslationsConfig.COMMAND_CREATE_SUCCESS, Placeholder.component("brew_name", brewItem.effectiveName())));
    }

    private BrewingStep getMix(Queue<String> arguments, CommandSender sender) {
        long cookTime = (long) (Double.parseDouble(arguments.poll()) * Moment.MINUTE);
        Map<Ingredient, Integer> ingredients = retrieveIngredients(arguments, sender);
        return new BrewingStep.Mix(new PassedMoment(cookTime), ingredients);
    }

    private Map<Ingredient, Integer> retrieveIngredients(Queue<String> arguments, CommandSender sender) {
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
        return BukkitIngredientManager.INSTANCE.getIngredientsWithAmount(ingredientStrings);
    }

    private BrewingStep getCook(Queue<String> arguments, CommandSender sender) {
        long cookTime = (long) (Double.parseDouble(arguments.poll()) * Moment.MINUTE);
        CauldronType cauldronType = Registry.CAULDRON_TYPE.get(BreweryKey.parse(arguments.poll()));
        return new BrewingStep.Cook(new PassedMoment(cookTime), retrieveIngredients(arguments, sender), cauldronType);
    }

    private BrewingStep getDistill(Queue<String> arguments, CommandSender sender) {
        int distillAmount = Integer.parseInt(arguments.poll());
        return new BrewingStep.Distill(distillAmount);
    }

    private BrewingStep getAge(Queue<String> arguments, CommandSender sender) {
        long age = (long) (Double.parseDouble(arguments.poll()) * Moment.AGING_YEAR);
        BarrelType barrelType = Registry.BARREL_TYPE.get(BreweryKey.parse(arguments.poll()));
        return new BrewingStep.Age(new PassedMoment(age), barrelType);
    }

    @Override
    public List<String> tabComplete(TheBrewingProject instance, CommandSender sender, OfflinePlayer offlineTarget, String label, List<String> args) {
        for (int i = args.size() - 2; i >= 0; i--) {
            if (TAB_COMPLETIONS.contains(args.get(i))) {
                int precedingArgsLength = args.size() - i - 1;
                return switch (REPLACEMENTS.getOrDefault(args.get(i), args.get(i))) {
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
                            throw new IllegalStateException("Unexpected value: " + REPLACEMENTS.getOrDefault(args.get(i), args.get(i)));
                };
            } else if (args.get(i).startsWith("-")) {
                return List.of();
            }
        }
        return TAB_COMPLETIONS;
    }
}
