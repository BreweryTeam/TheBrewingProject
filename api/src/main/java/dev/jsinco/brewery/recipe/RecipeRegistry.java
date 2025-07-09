package dev.jsinco.brewery.recipe;

import dev.jsinco.brewery.ingredient.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface RecipeRegistry<I> {

    Optional<Recipe<I>> getRecipe(@NotNull String recipeName);

    Collection<Recipe<I>> getRecipes();

    void registerRecipe(Recipe<I> recipe);

    void unRegisterRecipe(Recipe<I> recipe);

    Optional<RecipeResult<I>> getDefaultRecipe(@NotNull String recipeName);

    Collection<RecipeResult<I>> getDefaultRecipes();

    void registerDefaultRecipe(String name, RecipeResult<I> recipe);

    void unRegisterDefaultRecipe(String name);

    boolean isRegisteredIngredient(Ingredient ingredient);

    Set<Ingredient> registeredIngredients();
}
