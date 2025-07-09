package dev.jsinco.brewery.bukkit.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.ingredient.Ingredient;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IngredientsArgument implements CustomArgumentType.Converted<Map<Ingredient, Integer>, String> {

    private static final Pattern INGREDIENT_PATTERN = Pattern.compile("(([^ ,]+[ ,])*)([^ ,]*)");

    @Override
    public Map<Ingredient, Integer> convert(String nativeType) throws CommandSyntaxException {
        return BukkitIngredientManager.INSTANCE.getIngredientsWithAmount(
                Arrays.stream(nativeType.split("[, ]"))
                        .map(String::strip)
                        .filter(string -> !string.isBlank())
                        .toList()
        ).join();
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        if (builder.getRemainingLowerCase().isBlank()) {
            builder.suggest("\"");
            return builder.buildFuture();
        }
        if (!builder.getRemainingLowerCase().startsWith("\"")) {
            return builder.buildFuture();
        }
        Matcher matcher = INGREDIENT_PATTERN.matcher(builder.getRemainingLowerCase().substring(1));
        if (!matcher.matches()) {
            return builder.buildFuture();
        }
        String remainingLowerCase = matcher.group(3);
        String beforeRemaining = matcher.group(1);
        TheBrewingProject.getInstance().getRecipeRegistry().registeredIngredients().stream()
                .map(Ingredient::getKey)
                .map(ingredientKey -> ingredientKey.replaceAll("^minecraft:", ""))
                .filter(ingredientKey -> ingredientKey.startsWith(remainingLowerCase))
                .map(string -> "\"" + (beforeRemaining == null ? "" : beforeRemaining) + string)
                .forEach(string -> {
                    builder.suggest(string);
                });
        return builder.buildFuture();
    }
}
