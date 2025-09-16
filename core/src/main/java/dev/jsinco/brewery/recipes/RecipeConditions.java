package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.recipe.RecipeCondition;

import java.util.List;
import java.util.Map;

public class RecipeConditions {


    public static class NoCondition implements RecipeCondition {

        @Override
        public boolean matches(List<BrewingStep> expected, List<BrewingStep> actual) {
            return true;
        }
    }

    public record LastStep(BrewingStep.StepType stepType,
                           List<ScoreCondition> conditions) implements RecipeCondition {

        @Override
        public boolean matches(List<BrewingStep> expected, List<BrewingStep> actual) {
            if (expected.isEmpty() || actual.isEmpty()) {
                return false;
            }
            if (actual.getLast().stepType() != stepType) {
                return false;
            }
            return conditions().stream()
                    .allMatch(scoreCondition -> scoreCondition.matches(expected.getLast(), actual.getLast()));
        }
    }

    public interface ScoreCondition {

        boolean matches(BrewingStep expected, BrewingStep actual);
    }

    public record SingletonCondition(AmountCondition amountCondition, ScoreType type) implements ScoreCondition {

        @Override
        public boolean matches(BrewingStep expected, BrewingStep actual) {
            if (expected.getClass() != actual.getClass()) {
                return false;
            }
            return switch (type) {
                case TIME -> expected instanceof BrewingStep.TimedStep expectedTime
                        && actual instanceof BrewingStep.TimedStep actualTime
                        && amountCondition.matches(actualTime.time().moment(), expectedTime.time().moment());
                case INGREDIENTS, BARREL_TYPE -> false; // Unimplemented
                case DISTILL_AMOUNT -> expected instanceof BrewingStep.Distill expectedDistill
                        && actual instanceof BrewingStep.Distill actualDistill
                        && amountCondition.matches(actualDistill.runs(), expectedDistill.runs());
            };
        }
    }

    public record IngredientsCondition(Map<Ingredient, AmountCondition> conditions) implements ScoreCondition {
        @Override
        public boolean matches(BrewingStep expected, BrewingStep actual) {
            if (!(expected instanceof BrewingStep.IngredientsStep expectedIngredients)
                    || !(actual instanceof BrewingStep.IngredientsStep actualIngredients)) {
                return false;
            }
            for (Map.Entry<Ingredient, AmountCondition> condition : conditions.entrySet()) {
                int expectedAmount = expectedIngredients.ingredients().getOrDefault(condition.getKey(), -1);
                int actualAmount = actualIngredients.ingredients().getOrDefault(condition.getKey(), -1);
                if (expectedAmount == -1) {
                    continue;
                }
                if (actualAmount == -1) {
                    return false;
                }
                if (!condition.getValue().matches(actualAmount, expectedAmount)) {
                    return false;
                }
            }
            return true;
        }
    }

    public enum AmountCondition {
        EXCESSIVE,
        LACKING,
        ANY;

        public boolean matches(double value, double expected) {
            if (this == ANY) {
                return true;
            }
            if (this == LACKING) {
                return value < expected;
            }
            return value > expected;
        }
    }
}
