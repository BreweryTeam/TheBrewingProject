package dev.jsinco.brewery.util;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientGroup;
import dev.jsinco.brewery.api.ingredient.ScoredIngredient;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientUtil {

    private IngredientUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Map<Ingredient, Integer> sanitizeIngredients(Map<? extends Ingredient, Integer> ingredients) {
        Map<Ingredient, Integer> output = new HashMap<>();
        for (Map.Entry<? extends Ingredient, Integer> entry : ingredients.entrySet()) {
            Ingredient ingredient = entry.getKey();
            if (ingredient instanceof IngredientGroup ingredientGroup) {
                ingredient = ingredientGroup.alternatives().stream().max(Comparator.comparing(groupIngredient ->
                        groupIngredient instanceof ScoredIngredient scoredIngredient ? scoredIngredient.score() : 1D
                )).orElse(null);
            }
            if (ingredient == null) {
                continue;
            }
            output.put(ingredient, entry.getValue());
        }
        return output;
    }

    public static List<BrewingStep> sanitizeSteps(List<BrewingStep> steps) {
        return steps.stream()
                .map(IngredientUtil::sanitizeStep)
                .toList();
    }

    public static BrewingStep sanitizeStep(BrewingStep step) {
        return switch (step) {
            case BrewingStep.Mix mix -> mix.withIngredients(sanitizeIngredients(mix.ingredients()));
            case BrewingStep.Cook cook -> cook.withIngredients(sanitizeIngredients(cook.ingredients()));
            default -> step;
        };
    }
}
