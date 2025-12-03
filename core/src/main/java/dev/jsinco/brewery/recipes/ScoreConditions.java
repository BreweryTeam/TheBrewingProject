package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.recipe.ScoreCondition;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ScoreConditions {

    public record SingletonConditionImpl(AmountCondition amountCondition, ScoreType type) implements ScoreCondition.SingletonCondition {

        @Override
        public boolean matches(@Nullable BrewingStep expected, BrewingStep actual) {
            if (expected != null && expected.getClass() != actual.getClass()) {
                return false;
            }
            return switch (type) {
                case TIME -> actual instanceof BrewingStep.TimedStep actualTime
                        && amountCondition.matches(actualTime.time().moment(), expected == null ? -1D : ((BrewingStep.TimedStep) expected).time().moment());
                case INGREDIENTS, BARREL_TYPE -> false; // Unimplemented
                case DISTILL_AMOUNT -> expected instanceof BrewingStep.Distill expectedDistill
                        && actual instanceof BrewingStep.Distill actualDistill
                        && amountCondition.matches(actualDistill.runs(), expectedDistill.runs());
            };
        }
    }

    public record IngredientsConditionImpl(Map<Ingredient, AmountCondition> conditions) implements ScoreCondition.IngredientsCondition {
        @Override
        public boolean matches(@Nullable BrewingStep expected, BrewingStep actual) {
            if (!(actual instanceof BrewingStep.IngredientsStep actualIngredients)) {
                return false;
            }
            for (Map.Entry<Ingredient, AmountCondition> condition : conditions.entrySet()) {
                int actualAmount = actualIngredients.ingredients().getOrDefault(condition.getKey(), -1);
                if (!(expected instanceof BrewingStep.IngredientsStep expectedIngredients)) {
                    if (condition.getValue() != AmountCondition.ANY) {
                        return false;
                    }
                    continue;
                }
                int expectedAmount = expectedIngredients.ingredients().getOrDefault(condition.getKey(), -1);
                if (actualAmount == -1) {
                    return false;
                }
                if (expectedAmount == -1) {
                    if (condition.getValue() != AmountCondition.ANY) {
                        return false;
                    }
                    continue;
                }
                if (!condition.getValue().matches(actualAmount, expectedAmount)) {
                    return false;
                }
            }
            return true;
        }
    }
}
