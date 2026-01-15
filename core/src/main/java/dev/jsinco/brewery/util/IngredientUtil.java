package dev.jsinco.brewery.util;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.*;

import java.util.*;

public class IngredientUtil {

    private IngredientUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Map<BaseIngredient, Integer> sanitizeIngredients(Map<? extends Ingredient, Integer> ingredients) {
        Map<BaseIngredient, Integer> output = new HashMap<>();
        for (Map.Entry<? extends Ingredient, Integer> entry : ingredients.entrySet()) {
            Ingredient ingredient = entry.getKey();
            if (ingredient instanceof IngredientGroup ingredientGroup) {
                ingredient = ingredientGroup.alternatives().stream().max(Comparator.comparing(groupIngredient ->
                        IngredientUtil.score(groupIngredient).orElse(1D)
                )).orElse(null);
            }
            if (ingredient == null) {
                continue;
            }
            output.put(ingredient.toBaseIngredient(), entry.getValue());
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

    public static Optional<Double> score(Ingredient ingredient) {
        if (ingredient instanceof IngredientWithMeta ingredientWithMeta) {
            return Optional.ofNullable(ingredientWithMeta.get(IngredientMeta.SCORE));
        }
        return Optional.empty();
    }
}
