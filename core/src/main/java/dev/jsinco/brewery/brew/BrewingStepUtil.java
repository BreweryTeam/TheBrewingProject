package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.api.ingredient.*;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.util.IngredientUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BrewingStepUtil {

    public static double nearbyValueScore(long expected, long value) {
        double diff = Math.abs(expected - value);
        return 1 - Math.max(diff / expected, 0D);
    }

    public static double getIngredientsScore(Map<Ingredient, Integer> target, Map<Ingredient, Integer> actual) {
        Pair<Double, Integer> actualIngredientsScore = actual.entrySet()
                .stream()
                .flatMap(entry ->
                        IngredientUtil.score(entry.getKey()).map(score -> new Pair<>(score * entry.getValue(), entry.getValue())).stream()
                ).reduce(new Pair<>(0D, 0), (pair1, pair2) -> new Pair<>(pair1.first() + pair2.first(), pair1.second() + pair2.second()));
        Map<BaseIngredient, Integer> modifiedActual = IngredientUtil.sanitizeIngredients(actual);
        double ingredientScoreCumulativeSum = actualIngredientsScore.first();
        int scoredIngredientAmount = actualIngredientsScore.second();
        double output = 1D;
        for (Map.Entry<Ingredient, Integer> targetEntry : List.copyOf(target.entrySet())) {
            Ingredient ingredient = targetEntry.getKey();
            Pair<Integer, @Nullable Double> matchResult = computeScoreAndAmount(ingredient, modifiedActual, target, targetEntry.getValue());
            if (matchResult.first() == 0) {
                return 0;
            }
            Double score = matchResult.second();
            if (score != null) {
                ingredientScoreCumulativeSum += score * matchResult.first();
                scoredIngredientAmount += matchResult.first();
            }
            output *= nearbyValueScore(targetEntry.getValue(), matchResult.first());
        }
        if (!modifiedActual.isEmpty()) {
            return 0D;
        }
        double ingredientScore = ingredientScoreCumulativeSum > 0 ? ingredientScoreCumulativeSum / scoredIngredientAmount : 1D;
        return output * ingredientScore;
    }

    private static Pair<Integer, @Nullable Double> computeScoreAndAmount(Ingredient ingredient, Map<BaseIngredient, Integer> actual, Map<Ingredient, Integer> target, int targetAmount) {
        if (ingredient instanceof IngredientGroup ingredientGroup) {
            return computeIngredientGroupMatch(ingredientGroup, actual, target, targetAmount);
        }
        Optional<? extends Ingredient> ingredientMatchOptional = ingredient.findMatch(actual.keySet());
        if (ingredientMatchOptional.isPresent()) {
            Ingredient ingredientMatch = ingredientMatchOptional.get();
            int amount = actual.remove(ingredientMatch.toBaseIngredient());
            return new Pair<>(amount,
                    IngredientUtil.score(ingredientMatch)
                            .orElse(null)
            );
        }
        return new Pair<>(0, null);
    }

    private static Pair<Integer, @Nullable Double> computeIngredientGroupMatch(IngredientGroup ingredientGroup, Map<BaseIngredient, Integer> actual, Map<Ingredient, Integer> target, int targetAmount) {
        int ingredientAmount = 0;
        double ingredientScoreSum = 0D;
        int amountOfScoredIngredients = 0;
        List<Ingredient> postProcess = new ArrayList<>();
        for (Ingredient ingredient : ingredientGroup.alternatives()) {
            Optional<? extends Ingredient> optionalIngredient = ingredient.findMatch(actual.keySet());
            if (optionalIngredient.isEmpty()) {
                continue;
            }
            Ingredient ingredientMatch = optionalIngredient.get();
            BaseIngredient baseIngredient = ingredientMatch.toBaseIngredient();
            if (!actual.containsKey(baseIngredient)) {
                continue;
            }
            if (ingredientMatch instanceof IngredientWithMeta ingredientWithMeta) {
                Double score = ingredientWithMeta.get(IngredientMeta.SCORE);
                if (score != null) {
                    int amount = actual.get(baseIngredient);
                    ingredientScoreSum += score * amount;
                    amountOfScoredIngredients += amount;
                }
            }

            if (target.containsKey(ingredient)) {
                // Prioritize explicitly specified ingredients first
                postProcess.add(ingredient);
                continue;
            }
            ingredientAmount += actual.remove(baseIngredient);
        }
        for (Ingredient ingredient : postProcess) {
            if (targetAmount <= ingredientAmount) {
                break;
            }
            BaseIngredient baseIngredient = ingredient.toBaseIngredient();
            int amount = actual.getOrDefault(baseIngredient, 0);
            int increase = Math.min(amount, targetAmount - ingredientAmount);
            if (ingredient instanceof IngredientWithMeta ingredientWithMeta && ingredientWithMeta.get(IngredientMeta.SCORE) instanceof Double score) {
                ingredientScoreSum += score * increase;
                amountOfScoredIngredients += increase;
            }
            ingredientAmount += increase;
            if (amount == increase) {
                actual.remove(baseIngredient);
            } else {
                actual.put(baseIngredient, amount - increase);
            }
        }
        return new Pair<>(ingredientAmount, amountOfScoredIngredients == 0 ? null : ingredientScoreSum / amountOfScoredIngredients);
    }
}
