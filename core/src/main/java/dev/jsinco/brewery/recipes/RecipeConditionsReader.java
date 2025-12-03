package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.recipe.RecipeCondition;
import dev.jsinco.brewery.api.recipe.ScoreCondition;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.util.FutureUtil;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RecipeConditionsReader {

    private static final List<String> ALL_STEP_CONDITIONS = List.of(
            "final-step",
            "exact-step",
            "any-step",
            "first-step"
    );

    public static CompletableFuture<List<RecipeCondition>> fromConfigSection(ConfigurationSection section, IngredientManager<?> ingredientManager) {
        List<CompletableFuture<RecipeCondition>> recipeConditionFutures = new ArrayList<>();
        for (String stepKey : section.getKeys(false)) {
            ConfigurationSection configurationSection = section.getConfigurationSection(stepKey);
            Preconditions.checkArgument(configurationSection.contains("type"), "Unspecified step type for final-step");
            List<CompletableFuture<ScoreCondition>> scoreConditions = new ArrayList<>();
            for (String key : configurationSection.getKeys(false)) {
                if (key.equals("type")) {
                    continue;
                }
                ScoreType scoreType = Arrays.stream(ScoreType.values()).filter(score -> score.hasAlias(key))
                        .findAny().orElseThrow(() -> new IllegalArgumentException("Expected a valid score type, got: " + key));
                scoreConditions.add(switch (scoreType) {
                    case TIME, DISTILL_AMOUNT -> CompletableFuture.completedFuture(
                            parseSingletonCondition(configurationSection.getString(key), scoreType)
                    );
                    case INGREDIENTS ->
                            parseIngredientsCondition(configurationSection.getStringList(key), ingredientManager);
                    case BARREL_TYPE -> throw new UnsupportedOperationException("Unsupported score type: " + key);
                });
            }
            recipeConditionFutures.add(
                    FutureUtil.mergeFutures(scoreConditions)
                            .thenApplyAsync(conditions -> newRecipeCondition(conditions, stepKey, configurationSection))
            );
        }
        return FutureUtil.mergeFutures(recipeConditionFutures);
    }

    private static RecipeCondition newRecipeCondition(List<ScoreCondition> conditions, String stepKey, ConfigurationSection configurationSection) {
        BrewingStep.StepType stepType = BrewingStep.StepType.valueOf(configurationSection.getString("type").toUpperCase(Locale.ROOT));
        return switch (stepKey.toLowerCase(Locale.ROOT)) {
            case "final-step" -> new RecipeConditions.LastStepImpl(stepType, conditions);
            case "exact-step" ->
                    new RecipeConditions.ExactStepImpl(stepType, conditions, configurationSection.getInt("index"));
            case "any-step" -> new RecipeConditions.AnyStepImpl(stepType, conditions);
            case "first-step" -> new RecipeConditions.FirstStepImpl(stepType, conditions);
            default -> throw new IllegalArgumentException("Unknown recipe condition: " + stepKey);
        };
    }

    private static CompletableFuture<ScoreCondition> parseIngredientsCondition(List<String> stringList, IngredientManager<?> ingredientManager) {
        List<CompletableFuture<Pair<Optional<Ingredient>, ScoreCondition.AmountCondition>>> ingredientsFutures = new ArrayList<>();
        for (String string : stringList) {
            if (!string.contains("/")) {
                ingredientsFutures.add(ingredientManager.getIngredient(string)
                        .thenApplyAsync(ingredient -> {
                            if (ingredient.isEmpty()) {
                                Logger.logErr("Unknown ingredient: " + string);
                            }
                            return new Pair<>(ingredient, ScoreCondition.AmountCondition.ANY);
                        }));
            }
            String[] split = string.split("/", 2);
            ScoreCondition.AmountCondition.valueOf(split[1].toUpperCase(Locale.ROOT));
            ingredientsFutures.add(ingredientManager.getIngredient(split[0])
                    .thenApplyAsync(ingredient -> {
                        if (ingredient.isEmpty()) {
                            Logger.logErr("Unknown ingredient: " + string);
                        }
                        return new Pair<>(ingredient, ScoreCondition.AmountCondition.ANY);
                    }));
        }
        return FutureUtil.mergeFutures(ingredientsFutures)
                .thenApplyAsync(ingredients -> new ScoreConditions.IngredientsConditionImpl(
                        ingredients.stream()
                                .filter(pair -> pair.first().isPresent())
                                .collect(Collectors.toUnmodifiableMap(pair -> pair.first().get(), Pair::second))
                ));
    }

    private static ScoreCondition.SingletonCondition parseSingletonCondition(String amount, ScoreType type) {
        return new ScoreConditions.SingletonConditionImpl(ScoreCondition.AmountCondition.valueOf(amount.toUpperCase(Locale.ROOT)), type);
    }
}
