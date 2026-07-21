package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.recipe.RecipeCondition;
import dev.jsinco.brewery.api.recipe.ScoreCondition;
import dev.jsinco.brewery.util.BrewUtil;
import dev.jsinco.brewery.util.RegistryProviderHolder;
import org.jspecify.annotations.Nullable;

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
            for (List<BrewingStep> variation : BrewUtil.variations(actual, RegistryProviderHolder.instance().recipeRegistry())) {
                if (variation.getLast().stepType() != stepType) {
                    continue;
                }
                if (conditions().stream()
                        .allMatch(scoreCondition -> scoreCondition.matches(
                                sanitizeExpected(expected, variation.size() - 1),
                                variation.getLast()
                        ))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int complexity() {
            return 1 + conditions.stream()
                    .map(ScoreCondition::complexity)
                    .reduce(0, Integer::sum);
        }
    }

    public record ExactStepImpl(BrewingStep.StepType stepType, List<ScoreCondition> conditions,
                                int index) implements RecipeCondition.ExactStep {

        @Override
        public boolean matches(@Nullable List<BrewingStep> expected, List<BrewingStep> actual) {
            if (actual.isEmpty()) {
                return false;
            }
            for (List<BrewingStep> variation : BrewUtil.variations(actual, RegistryProviderHolder.instance().recipeRegistry())) {
                if (variation.size() <= index || actual.get(index).stepType() != stepType) {
                    continue;
                }
                if (conditions().stream()
                        .allMatch(scoreCondition -> scoreCondition.matches(
                                sanitizeExpected(expected, index),
                                actual.get(index)
                        ))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int complexity() {
            return 1 + conditions.stream()
                    .map(ScoreCondition::complexity)
                    .reduce(0, Integer::sum);
        }
    }

    public record AnyStepImpl(BrewingStep.StepType stepType,
                              List<ScoreCondition> conditions) implements RecipeCondition.AnyStep {


        @Override
        public boolean matches(@Nullable List<BrewingStep> expected, List<BrewingStep> actual) {
            if (actual.isEmpty()) {
                return false;
            }
            for (List<BrewingStep> variation : BrewUtil.variations(actual, RegistryProviderHolder.instance().recipeRegistry())) {
                for (int i = 0; i < variation.size(); i++) {
                    final int index = i;
                    if (variation.get(index).stepType() == stepType && conditions.stream()
                            .allMatch(scoreCondition -> scoreCondition.matches(
                                    sanitizeExpected(expected, index),
                                    variation.get(index)
                            ))
                    ) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int complexity() {
            return 1 + conditions.stream()
                    .map(ScoreCondition::complexity)
                    .reduce(0, Integer::sum);
        }
    }

    public record FirstStepImpl(BrewingStep.StepType stepType,
                                List<ScoreCondition> conditions) implements RecipeCondition.FirstStep {

        @Override
        public boolean matches(@Nullable List<BrewingStep> expected, List<BrewingStep> actual) {
            if (actual.isEmpty()) {
                return false;
            }
            for (List<BrewingStep> variation : BrewUtil.variations(actual, RegistryProviderHolder.instance().recipeRegistry())) {
                if (variation.getFirst().stepType() != stepType) {
                    continue;
                }
                if (conditions().stream()
                        .allMatch(scoreCondition -> scoreCondition.matches(
                                sanitizeExpected(expected, 0),
                                actual.getFirst()
                        ))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int complexity() {
            return 1 + conditions.stream()
                    .map(ScoreCondition::complexity)
                    .reduce(0, Integer::sum);
        }
    }

}
