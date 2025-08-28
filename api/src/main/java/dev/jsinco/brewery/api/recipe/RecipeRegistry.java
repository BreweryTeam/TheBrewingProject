package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Some recipes are loaded on a delay, therefore <b>NO RECIPE SHOULD BE ACCESSED ON STARTUP</b>
 *
 * @param <I> An item stack type
 */
public interface RecipeRegistry<I> {

    /**
     * @param recipeName The name of the recipe
     * @return An optional recipe, depending on if it existed
     */
    Optional<Recipe<I>> getRecipe(@NotNull String recipeName);

    /**
     * @return All recipes registered at the moment
     */
    Collection<Recipe<I>> getRecipes();

    /**
     * @param recipe The recipe to register
     */
    void registerRecipe(Recipe<I> recipe);

    /**
     * @param recipe The recipe to unregister
     */
    void unRegisterRecipe(Recipe<I> recipe);

    /**
     * @param recipeName The key of the default recipe
     * @return A recipe result for the default recipe
     */
    Optional<RecipeResult<I>> getDefaultRecipe(@NotNull String recipeName);

    /**
     * @return All default recipes
     */
    Collection<RecipeResult<I>> getDefaultRecipes();

    /**
     * @param name   The name of the default recipe
     * @param recipe The default recipe
     */
    void registerDefaultRecipe(String name, RecipeResult<I> recipe);

    /**
     * @param name The name of the default recipe
     */
    void unRegisterDefaultRecipe(String name);

    /**
     * @param ingredient The ingredient to check whether it is registered
     * @return True if this ingredient is used in a recipe
     */
    boolean isRegisteredIngredient(Ingredient ingredient);

    /**
     * @return All ingredients registered in a recipe
     */
    Set<Ingredient> registeredIngredients();
}
