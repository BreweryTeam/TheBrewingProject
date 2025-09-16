package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.recipe.RecipeCondition;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.util.FutureUtil;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RecipeConditionsReader {

    public static CompletableFuture<RecipeCondition> fromConfigSection(ConfigurationSection section, IngredientManager<?> ingredientManager) {
        if (!section.contains("final-step")) {
            return CompletableFuture.completedFuture(new RecipeConditions.NoCondition());
        }
        Preconditions.checkArgument(section.isConfigurationSection("final-step"), "Expected final-step to be a configuration section");
        ConfigurationSection configurationSection = section.getConfigurationSection("final-step");
        Preconditions.checkArgument(configurationSection.contains("type"));
        BrewingStep.StepType stepType = BrewingStep.StepType.valueOf(configurationSection.getString("type"));
        List<CompletableFuture<RecipeConditions.ScoreCondition>> scoreConditions = new ArrayList<>();
        for (String key : configurationSection.getKeys(false)) {
            if (key.equals("type")) {
                continue;
            }
            ScoreType scoreType = Arrays.stream(ScoreType.values()).filter(score -> score.hasAlias(key))
                    .findAny().orElseThrow(() -> new IllegalArgumentException("Expected a valid score type, got: " + key));
            scoreConditions.add(switch (scoreType) {
                case TIME, DISTILL_AMOUNT -> CompletableFuture.completedFuture(new RecipeConditions.SingletonCondition(
                        RecipeConditions.AmountCondition.valueOf(configurationSection.getString(key).toUpperCase(Locale.ROOT)),
                        scoreType
                ));
                case INGREDIENTS ->
                        parseIngredientsCondition(configurationSection.getStringList(key), ingredientManager);
                case BARREL_TYPE -> throw new UnsupportedOperationException("Unsupported score type: " + key);
            });
        }
        return FutureUtil.mergeFutures(scoreConditions)
                .thenApplyAsync(conditions -> new RecipeConditions.LastStep(stepType, conditions));
    }

    private static CompletableFuture<RecipeConditions.ScoreCondition> parseIngredientsCondition(List<String> stringList, IngredientManager<?> ingredientManager) {
        List<CompletableFuture<Pair<Optional<Ingredient>, RecipeConditions.AmountCondition>>> ingredientsFutures = new ArrayList<>();
        for (String string : stringList) {
            if (!string.contains("/")) {
                ingredientsFutures.add(ingredientManager.getIngredient(string)
                        .thenApplyAsync(ingredient -> {
                            if (ingredient.isEmpty()) {
                                Logger.logErr("Unknown ingredient: " + string);
                            }
                            return new Pair<>(ingredient, RecipeConditions.AmountCondition.ANY);
                        }));
            }
            String[] split = string.split("/", 2);
            RecipeConditions.AmountCondition.valueOf(split[1].toUpperCase(Locale.ROOT));
            ingredientsFutures.add(ingredientManager.getIngredient(split[0])
                    .thenApplyAsync(ingredient -> {
                        if (ingredient.isEmpty()) {
                            Logger.logErr("Unknown ingredient: " + string);
                        }
                        return new Pair<>(ingredient, RecipeConditions.AmountCondition.ANY);
                    }));
        }
        return FutureUtil.mergeFutures(ingredientsFutures)
                .thenApplyAsync(ingredients -> new RecipeConditions.IngredientsCondition(
                        ingredients.stream()
                                .filter(pair -> pair.first().isPresent())
                                .collect(Collectors.toUnmodifiableMap(pair -> pair.first().get(), Pair::second))
                ));
    }
}
