package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import org.jetbrains.annotations.Range;

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

    /**
     * @param score The score to use with the ingredient range (0, 1] (0 value not allowed)
     * @return An ingredient representing this recipe output item
     */
    Ingredient toIngredient(@Range(from = 0, to = 1) double score);

    /**
     *
     * @param steps The brewing procedure
     * @return The brew score
     */
    BrewScore score(List<BrewingStep> steps);
}
