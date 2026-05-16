package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewScore;
import dev.jsinco.brewery.api.ingredient.UncheckedIngredient;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.List;

/**
 * @param <I> An item stack type
 */
public interface RecipeResult<I> {

    /**
     *
     * @param score The score of the brew
     * @param brew  The brew
     * @param state The state of the brew
     * @return A new item from this recipe result
     * @deprecated Use {@link RecipeMatcherResult} instead
     */
    @Deprecated(forRemoval = true)
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
     * @return The color of the recipe result
     */
    Color color();

    /**
     * @return The lore specified in the config
     */
    List<Component> staticLore();

    /**
     * @return True if the output item will have glint effect
     */
    boolean glint();

    /**
     * @return The custom model data for the output item
     */
    int customModelData();

    /**
     * @return The item model for the item, or null if none is spefied
     */
    @Nullable Key itemModel();

    /**
     * @return The output item override, or null if none is specified
     */
    @Nullable UncheckedIngredient itemOutputOverride();

    /**
     * @return The name of the brew in serialized mini message format
     */
    String name();

    /**
     *
     * @return The lore of the brew as defined in the recipe in mini message format.
     */
    List<String> lore();

    /**
     * @return The recipe effects of the recipe result
     */
    RecipeEffects recipeEffects();

    /**
     * @return True if more lore should be appended to the brew other than the one specified in the recipe
     */
    boolean appendBrewInfoLore();
}
