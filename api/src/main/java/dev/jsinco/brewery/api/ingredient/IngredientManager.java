package dev.jsinco.brewery.api.ingredient;

import dev.jsinco.brewery.api.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Get an instance of an ingredient from an ItemStack or a string.
 * Used for cauldrons and loading for loading recipes.
 *
 * @param <I> Item stack
 */
public interface IngredientManager<I> {

    /**
     * Don't use this method on startup, expect unexpected behavior if you do
     *
     * @param itemStack An item stack
     * @return An ingredient of the item stack
     */
    Ingredient getIngredient(@NotNull I itemStack);

    /**
     * Pretty much all items plugins initialize items on a delay. This is therefore a necessary measure to use on enable
     * <p>
     * Startup friendly :)
     * </p>
     *
     * @param ingredientStr A string representing the ingredient
     * @return A completable future with an optionally present ingredient, if
     */
    CompletableFuture<Optional<Ingredient>> getIngredient(@NotNull String ingredientStr);

    /**
     * @param ingredientStr A string with the format [ingredient-name]/[runs]. Allows not specifying runs, where it will default to 1
     * @return An ingredient/runs pair
     * @throws IllegalArgumentException if the ingredients string is invalid
     */
    CompletableFuture<Pair<Ingredient, Integer>> getIngredientWithAmount(String ingredientStr) throws IllegalArgumentException;

    /**
     * Parse a list of strings into a map of ingredients with runs
     *
     * @param stringList A list of strings with valid formatting, see {@link #getIngredientWithAmount(String)}
     * @return A map representing ingredients with runs
     * @throws IllegalArgumentException if there's any invalid ingredient string
     */
    CompletableFuture<Map<Ingredient, Integer>> getIngredientsWithAmount(List<String> stringList) throws IllegalArgumentException;

    /**
     * Utility method, merge ingredients amount of both maps
     *
     * @param mutableIngredientsMap A map of ingredients with amount
     * @param ingredients           A map of ingredients with amount
     */
    static void merge(Map<Ingredient, Integer> mutableIngredientsMap, Map<Ingredient, Integer> ingredients) {
        for (Map.Entry<Ingredient, Integer> ingredient : ingredients.entrySet()) {
            insertIngredientIntoMap(mutableIngredientsMap, new Pair<>(ingredient.getKey(), ingredient.getValue()));
        }
    }

    /**
     * Utility method, insert ingredient into a map of ingredients with amount
     *
     * @param mutableIngredientsMap Ingredients map with amounts
     * @param ingredient            A pair with ingredient and amounts
     */
    static void insertIngredientIntoMap(Map<Ingredient, Integer> mutableIngredientsMap, Pair<Ingredient, Integer> ingredient) {
        int amount = mutableIngredientsMap.computeIfAbsent(ingredient.first(), ignored -> 0);
        mutableIngredientsMap.put(ingredient.first(), amount + ingredient.second());
    }
}
