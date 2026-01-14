package dev.jsinco.brewery.bukkit.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.bukkit.util.BukkitMessageUtil;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IngredientsArgument implements CustomArgumentType.Converted<Map<Ingredient, Integer>, String> {

    private static final Pattern INGREDIENTS_PATTERN = Pattern.compile("(([^ ,]+[ ,] ?)*)([^ ,]*)");
    private static final SimpleCommandExceptionType TIMEOUT = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("Command timed out, another integrated plugin probably failed on startup"))
    );
    private static final SimpleCommandExceptionType INVALID_SYNTAX = new SimpleCommandExceptionType(
            BukkitMessageUtil.toBrigadier("tbp.command.invalid-ingredient-syntax")
    );

    @Override
    public Map<Ingredient, Integer> convert(String nativeType) throws CommandSyntaxException {
        try {
            return BukkitIngredientManager.INSTANCE.getIngredientsWithAmount(
                            splitIngredients(nativeType).stream()
                                    .map(String::strip)
                                    .filter(string -> !string.isBlank())
                                    .toList(), true
                    ).orTimeout(50, TimeUnit.MILLISECONDS)
                    .join();
        } catch (CompletionException e) {
            if (e.getCause() != null && e.getCause() instanceof TimeoutException) {
                throw TIMEOUT.create();
            }
            throw e;
        }
    }

    private static List<String> splitIngredients(String input) throws CommandSyntaxException {
        if (input.isEmpty()) {
            return List.of();
        }
        StringBuilder builder = new StringBuilder();
        List<String> output = new ArrayList<>();
        int curlyBracketsDepth = 0;
        for (char character : input.toCharArray()) {
            if (curlyBracketsDepth == 0 && (character == ' ' || character == ',')) {
                String next = builder.toString();
                builder = new StringBuilder();
                if (!next.isEmpty()) {
                    output.add(next);
                }
                continue;
            }
            if (character == '{') {
                curlyBracketsDepth++;
            }
            if (character == '}') {
                curlyBracketsDepth--;
                if (curlyBracketsDepth < 0) {
                    throw INVALID_SYNTAX.create();
                }
            }
            builder.append(character);
        }
        if (curlyBracketsDepth > 0) {
            throw INVALID_SYNTAX.create();
        }
        if (!builder.isEmpty()) {
            output.add(builder.toString());
        }
        return output;
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
        Matcher matcher = INGREDIENTS_PATTERN.matcher(builder.getRemainingLowerCase().substring(1));
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
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
