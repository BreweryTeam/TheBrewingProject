package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.ResolvedIngredientManager;
import dev.jsinco.brewery.api.recipe.RecipeCondition;
import dev.jsinco.brewery.api.recipe.ScoreCondition;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.api.util.Pair;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecipeConditionsReader {

    public static List<RecipeCondition> fromConfigSection(ConfigurationSection section, ResolvedIngredientManager<?> ingredientManager) {
        List<RecipeCondition> recipeConditions = new ArrayList<>();
        for (String stepKey : section.getKeys(false)) {
            if (!section.isConfigurationSection(stepKey)) {
                continue;
            }
            ConfigurationSection configurationSection = section.getConfigurationSection(stepKey);
            Preconditions.checkArgument(configurationSection.contains("type"), "Unspecified step type for final-step");
            List<ScoreCondition> scoreConditions = new ArrayList<>();
            for (String key : configurationSection.getKeys(false)) {
                if (key.equals("type")) {
                    continue;
                }
                ScoreType scoreType = Arrays.stream(ScoreType.values()).filter(score -> score.hasAlias(key))
                        .findAny().orElseThrow(() -> new IllegalArgumentException("Expected a valid score type, got: " + key));
                scoreConditions.add(switch (scoreType) {
                    case TIME, DISTILL_AMOUNT ->
                            parseSingletonCondition(configurationSection.getString(key), scoreType);
                    case INGREDIENTS ->
                            parseIngredientsCondition(configurationSection.getStringList(key), ingredientManager);
                    case BARREL_TYPE -> throw new UnsupportedOperationException("Unsupported score type: " + key);
                });
            }
            recipeConditions.add(
                    newRecipeCondition(scoreConditions, stepKey, configurationSection)
            );
        }
        return recipeConditions;
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

    private static ScoreCondition parseIngredientsCondition(List<String> stringList, ResolvedIngredientManager<?> ingredientManager) {
        List<Pair<Ingredient, ScoreCondition.AmountCondition>> ingredientsFutures = new ArrayList<>();
        for (String string : stringList) {
            if (!string.contains("/")) {
                Optional<Ingredient> ingredient = ingredientManager.getIngredient(string);
                if (ingredient.isEmpty()) {
                    Logger.logErr("Unknown ingredient: " + string);
                    continue;
                }
                ingredientsFutures.add(new Pair<>(ingredient.get(), ScoreCondition.AmountCondition.ANY));
            }
            String[] split = string.split("/", 2);
            ScoreCondition.AmountCondition amountCondition = ScoreCondition.AmountCondition.valueOf(split[1].toUpperCase(Locale.ROOT));
            Optional<Ingredient> ingredient = ingredientManager.getIngredient(split[0]);
            if (ingredient.isEmpty()) {
                Logger.logErr("Unknown ingredient: " + string);
                continue;
            }
            ingredientsFutures.add(new Pair<>(ingredient.get(), amountCondition));
        }
        return new ScoreConditions.IngredientsConditionImpl(ingredientsFutures
                .stream()
                .collect(Collectors.toMap(Pair::first, Pair::second))
        );

    }

    private static ScoreCondition.SingletonCondition parseSingletonCondition(String amount, ScoreType type) {
        return new ScoreConditions.SingletonConditionImpl(ScoreCondition.AmountCondition.valueOf(amount.toUpperCase(Locale.ROOT)), type);
    }
}
