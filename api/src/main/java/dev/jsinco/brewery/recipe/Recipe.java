package dev.jsinco.brewery.recipe;

import dev.jsinco.brewery.brew.BrewQuality;
import dev.jsinco.brewery.brew.BrewingStep;

import java.util.List;

public interface Recipe<I> {

    /**
     * @return The name of the recipe
     */
    String getRecipeName();

    /**
     * @return The difficulty of the recipe
     */
    double getBrewDifficulty();

    /**
     * @return The brewing steps of the recipe
     */
    List<BrewingStep> getSteps();

    /**
     * @return Quality factored recipe results
     */
    QualityData<RecipeResult<I>> getRecipeResults();

    /**
     * @param quality A brew quality
     * @return The recipe result for specified quality
     */
    default RecipeResult<I> getRecipeResult(BrewQuality quality) {
        return getRecipeResults().get(quality);
    }
}
