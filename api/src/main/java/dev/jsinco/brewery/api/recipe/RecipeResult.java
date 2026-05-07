package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewScore;
import net.kyori.adventure.text.Component;

import java.awt.Color;

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

    /**
     * @return The recipe item without any extra lore
     */
    I newLorelessItem();

    /**
     * @return The effects of this recipe result
     */
    RecipeEffects effects();

    /**
     * @return The display name of the output item
     */
    Component displayName();

    /**
     * Note that this might be unused for custom items
     *
     * @return The color linked to this brew.
     */
    Color brewColor();

}
