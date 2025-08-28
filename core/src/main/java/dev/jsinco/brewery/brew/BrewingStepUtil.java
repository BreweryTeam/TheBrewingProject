package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.ingredient.ScoredIngredient;
import dev.jsinco.brewery.api.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrewingStepUtil {

    public static double nearbyValueScore(long expected, long value) {
        double diff = Math.abs(expected - value);
        return 1 - Math.max(diff / expected, 0D);
    }

    public static double getIngredientsScore(Map<Ingredient, Integer> target, Map<Ingredient, Integer> actual) {
        List<Pair<Double, Integer>> customScores = actual.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof ScoredIngredient)
                .map(entry -> new Pair<>(((ScoredIngredient) entry.getKey()).score(), entry.getValue()))
                .toList();
        Pair<Double, Integer> scoredIngredientPair = customScores.stream().reduce(new Pair<>(1D, 1),
                (pair1, pair2) -> new Pair<>(pair1.first() * Math.pow(pair2.first(), pair2.second()), pair1.second() + pair2.second()));
        // Average out t
        double output = Math.pow(scoredIngredientPair.first(), (double) 1 / scoredIngredientPair.second());
        Map<Ingredient, Integer> modifiedTarget = compressIngredients(target);
        Map<Ingredient, Integer> modifiedActual = compressIngredients(actual);

        if (modifiedTarget.size() != modifiedActual.size()) {
            return 0;
        }
        for (Map.Entry<Ingredient, Integer> targetEntry : modifiedTarget.entrySet()) {
            Integer actualAmount = modifiedActual.get(targetEntry.getKey());
            if (actualAmount == null || actualAmount == 0) {
                return 0;
            }
            output *= nearbyValueScore(targetEntry.getValue(), actualAmount);
        }
        return output;
    }

    private static Map<Ingredient, Integer> compressIngredients(Map<Ingredient, Integer> ingredients) {
        Map<Ingredient, Integer> output = new HashMap<>();
        ingredients.entrySet()
                .stream()
                .map(entry -> {
                    if (entry.getKey() instanceof ScoredIngredient scoredIngredient) {
                        return new Pair<>(scoredIngredient.baseIngredient(), entry.getValue());
                    } else {
                        return new Pair<>(entry.getKey(), entry.getValue());
                    }
                })
                .forEach(pair -> IngredientManager.insertIngredientIntoMap(output, pair));
        return output;
    }
}
