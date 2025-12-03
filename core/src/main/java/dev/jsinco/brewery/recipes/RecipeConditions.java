package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.recipe.RecipeCondition;
import dev.jsinco.brewery.api.recipe.ScoreCondition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeConditions {

    static @Nullable BrewingStep sanitizeExpected(@Nullable List<BrewingStep> expected, int index) {
        if (expected == null) {
            return null;
        }
        return expected.size() > index ? expected.get(index) : null;
    }

    public record LastStepImpl(BrewingStep.StepType stepType,
                               List<ScoreCondition> conditions) implements RecipeCondition.LastStep {

        @Override
        public boolean matches(@Nullable List<BrewingStep> expected, List<BrewingStep> actual) {
            if (actual.isEmpty()) {
                return false;
            }
            if (actual.getLast().stepType() != stepType) {
                return false;
            }
            return conditions().stream()
                    .allMatch(scoreCondition -> scoreCondition.matches(
                            sanitizeExpected(expected, actual.size() - 1),
                            actual.getLast()
                    ));
        }

        @Override
        public int complexity() {
            return 1 + conditions.size();
        }
    }

    public record ExactStepImpl(BrewingStep.StepType stepType, List<ScoreCondition> conditions,
                                int index) implements RecipeCondition.ExactStep {

        @Override
        public boolean matches(@Nullable List<BrewingStep> expected, List<BrewingStep> actual) {
            if (actual.isEmpty()) {
                return false;
            }
            if (actual.size() <= index) {
                return false;
            }
            if (actual.get(index).stepType() != stepType) {
                return false;
            }
            return conditions().stream()
                    .allMatch(scoreCondition -> scoreCondition.matches(
                            sanitizeExpected(expected, index),
                            actual.get(index)
                    ));
        }

        @Override
        public int complexity() {
            return 1 + conditions.size();
        }
    }

    public record AnyStepImpl(BrewingStep.StepType stepType,
                          List<ScoreCondition> conditions) implements RecipeCondition.AnyStep {


        @Override
        public boolean matches(@Nullable List<BrewingStep> expected, List<BrewingStep> actual) {
            if (actual.isEmpty()) {
                return false;
            }
            for (int i = 0; i < actual.size(); i++) {
                final int index = i;
                if (actual.get(index).stepType() == stepType && conditions.stream()
                        .allMatch(scoreCondition -> scoreCondition.matches(
                                sanitizeExpected(expected, index),
                                actual.get(index)
                        ))
                ) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int complexity() {
            return 1 + conditions.size();
        }
    }

    public record FirstStepImpl(BrewingStep.StepType stepType, List<ScoreCondition> conditions) implements RecipeCondition.FirstStep {

        @Override
        public boolean matches(@Nullable List<BrewingStep> expected, List<BrewingStep> actual) {
            if (actual.isEmpty()) {
                return false;
            }
            if (actual.getFirst().stepType() != stepType) {
                return false;
            }
            return conditions().stream()
                    .allMatch(scoreCondition -> scoreCondition.matches(
                            sanitizeExpected(expected, 0),
                            actual.getFirst()
                    ));
        }

        @Override
        public int complexity() {
            return 1 + conditions.size();
        }
    }

}
