package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.brew.BrewingStep;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface RecipeCondition {

    boolean matches(@Nullable List<BrewingStep> expected, List<BrewingStep> actual);

    int complexity();

    interface LastStep extends RecipeCondition {

        BrewingStep.StepType stepType();

        List<ScoreCondition> conditions();
    }

    interface ExactStep extends RecipeCondition {

        BrewingStep.StepType stepType();

        int index();

        List<ScoreCondition> conditions();
    }

    interface AnyStep extends RecipeCondition {

        BrewingStep.StepType stepType();

        List<ScoreCondition> conditions();
    }

    interface FirstStep extends RecipeCondition {

        BrewingStep.StepType stepType();

        List<ScoreCondition> conditions();
    }
}
