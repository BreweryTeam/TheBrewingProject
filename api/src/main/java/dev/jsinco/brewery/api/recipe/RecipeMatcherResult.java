package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.brew.BrewingStep;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Create one with a {@link RecipeMatcher}
 *
 * @param <I> The item type
 */
public interface RecipeMatcherResult<I> {

    /**
     * @return a complete brew item from the match
     */
    I toItem(Brew.State state);

    /**
     * @param overrideQuality The quality to create the item with, null is failed
     * @return a complete item from the match
     */
    I toItem(Brew.State state, @Nullable BrewQuality overrideQuality);

    /**
     * @param preferredDefaultRecipe The preferred default recipe
     * @return a complete brew item from the match
     */
    I toItem(Brew.State state, @Nullable DefaultRecipe<I> preferredDefaultRecipe);

    /**
     * @return A brew item without lore
     */
    I toLorelessItem(Brew.State state);

    /**
     *
     * @param overrideQuality The quality to create the item with, null is failed
     * @return A brew item without lore
     */
    I toLorelessItem(Brew.State state, @Nullable BrewQuality overrideQuality);

    /**
     *
     * @return The recipe that matched with the item, if present
     */
    Optional<Recipe<I>> recipeMatch();

    /**
     *
     * @return The quality of for the match against the nearest recipe, if present
     */
    Optional<BrewQuality> quality();

    /**
     * @return The score of the recipe match
     */
    BrewScore score();

    /**
     * @return The recipe result to be used when creating a brew item, if present
     */
    Optional<RecipeResult<I>> recipeResult();

    /**
     * @return The step variation matched against
     */
    List<BrewingStep> matchingSteps();
}
