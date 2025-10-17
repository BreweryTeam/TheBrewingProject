package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientGroup;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.ingredient.ScoredIngredient;
import dev.jsinco.brewery.api.util.Pair;
import org.jspecify.annotations.Nullable;

import java.util.*;

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

        List<@Nullable Double> ingredientScores = new ArrayList<>();
        for (Map.Entry<Ingredient, Integer> targetEntry : List.copyOf(modifiedTarget.entrySet())) {
            Ingredient ingredient = targetEntry.getKey();
            int actualAmount;
            if (ingredient instanceof IngredientGroup ingredientGroup) {
                Pair<Integer, Double> ingredientGroupMatch = computeIngredientGroupMatch(ingredientGroup, modifiedActual, modifiedTarget, targetEntry.getValue());
                actualAmount = ingredientGroupMatch.first();
                ingredientScores.add(ingredientGroupMatch.second());
            } else {
                actualAmount = modifiedActual.containsKey(ingredient) ? modifiedActual.remove(ingredient) : 0;
            }
            if (actualAmount == 0) {
                return 0;
            }
            output *= nearbyValueScore(targetEntry.getValue(), actualAmount);
        }
        if(!modifiedActual.isEmpty()){
            return 0D;
        }
        double ingredientScore = ingredientScores.isEmpty() ? 1D : ingredientScores.stream()
                .filter(Objects::nonNull)
                .reduce(1D, (aDouble, aDouble2) -> aDouble * aDouble2);
        return output * ingredientScore;
    }

    private static Pair<Integer, @Nullable Double> computeIngredientGroupMatch(IngredientGroup ingredientGroup, Map<Ingredient, Integer> modifiedActual, Map<Ingredient, Integer> targetIngredients, int target) {
        int ingredientAmount = 0;
        double ingredientScoreSum = 0D;
        int amountOfScoredIngredients = 0;
        List<Ingredient> postProcess = new ArrayList<>();
        for (Ingredient ingredient : ingredientGroup.alternatives()) {
            if (ingredient instanceof ScoredIngredient(Ingredient baseIngredient, double score)) {
                ingredient = baseIngredient;
                if (!targetIngredients.containsKey(ingredient) && modifiedActual.containsKey(ingredient)) {
                    int amount = modifiedActual.get(ingredient);
                    ingredientScoreSum += score * amount;
                    amountOfScoredIngredients += amount;
                }
            }
            if (!modifiedActual.containsKey(ingredient)) {
                continue;
            }
            if (targetIngredients.containsKey(ingredient)) {
                // Prioritize explicitly specified ingredients first
                postProcess.add(ingredient);
                continue;
            }
            ingredientAmount += modifiedActual.remove(ingredient);
        }
        for (Ingredient ingredient : postProcess) {
            if (target <= ingredientAmount) {
                break;
            }

            int amount = modifiedActual.getOrDefault(ingredient, 0);
            int increase = Math.min(amount, target - ingredientAmount);
            if (ingredient instanceof ScoredIngredient(Ingredient baseIngredient, double score)) {
                ingredientScoreSum += score * increase;
                ingredient = baseIngredient;
                amountOfScoredIngredients += increase;
            }
            ingredientAmount += increase;
            if (amount == increase) {
                modifiedActual.remove(ingredient);
            } else {
                modifiedActual.put(ingredient, amount - increase);
            }
        }
        return new Pair<>(ingredientAmount, amountOfScoredIngredients == 0 ? null : ingredientScoreSum / amountOfScoredIngredients);
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
