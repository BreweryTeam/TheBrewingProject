package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.recipe.RecipeCondition;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class RecipeConditions {


    public static class NoCondition implements RecipeCondition {

        @Override
        public boolean matches(@Nullable List<BrewingStep> expected, List<BrewingStep> actual) {
            return true;
        }

        @Override
        public int complexity() {
            return 0;
        }
    }

    public record LastStep(BrewingStep.StepType stepType,
                           List<ScoreCondition> conditions) implements RecipeCondition {

        @Override
        public boolean matches(@Nullable List<BrewingStep> expected, List<BrewingStep> actual) {
            if (actual.isEmpty()) {
                return false;
            }
            if (actual.getLast().stepType() != stepType) {
                return false;
            }
            return conditions().stream()
                    .allMatch(scoreCondition -> scoreCondition.matches(expected == null ? null : expected.get(actual.size() - 1), actual.getLast()));
        }

        @Override
        public int complexity() {
            return 1 + conditions.size();
        }
    }

    public interface ScoreCondition {

        boolean matches(@Nullable BrewingStep expected, BrewingStep actual);
    }

    public record SingletonCondition(AmountCondition amountCondition, ScoreType type) implements ScoreCondition {

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

    public record IngredientsCondition(Map<Ingredient, AmountCondition> conditions) implements ScoreCondition {
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

    public enum AmountCondition {
        EXCESSIVE,
        LACKING,
        ANY;

        public boolean matches(double value, double expected) {
            if (this == ANY) {
                return true;
            }
            if (expected == -1D) {
                return false;
            }
            if (this == LACKING) {
                return value < expected;
            }
            return value > expected;
        }
    }
}
