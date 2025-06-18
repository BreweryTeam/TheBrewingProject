package dev.jsinco.brewery.recipe;

import dev.jsinco.brewery.brew.BrewQuality;
import dev.jsinco.brewery.brew.BrewingStep;

import java.util.List;

public interface Recipe<I> {

    String getRecipeName();

    double getBrewDifficulty();

    List<BrewingStep> getSteps();

    QualityData<RecipeResult<I>> getRecipeResults();

    default RecipeResult<I> getRecipeResult(BrewQuality quality) {
        return getRecipeResults().get(quality);
    }
}
