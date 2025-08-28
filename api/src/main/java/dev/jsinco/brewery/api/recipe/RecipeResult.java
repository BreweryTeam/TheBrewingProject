package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewScore;

/**
 * @param <I> An item stack type
 */
public interface RecipeResult<I> {

    /**
     * @param score The score of the brew
     * @param brew  The brew
     * @param state The state of the brew
     * @return A new item from this recipe result
     */
    I newBrewItem(BrewScore score, Brew brew, Brew.State state);
}
