package dev.jsinco.brewery.api.ingredient;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.api.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Get an instance of an ingredient from an ItemStack or a string.
 * Used for cauldrons and loading for loading recipes.
 *
 * @param <I> Item stack
 */
public interface IngredientManager<I> {

    Pattern INGREDIENT_META_DATA_RE = Pattern.compile("\\{(.*)\\}");
    Pattern INGREDIENT_META_DATA_ELEMENT_RE = Pattern.compile("([^,{}]+)=(.+)");

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
     * @return A completable future with an optionally present ingredient
     */
    CompletableFuture<Optional<Ingredient>> getIngredient(@NotNull String ingredientStr);

    /**
     * Deserialize the ingredient with meta data. Meta data is within curly brackets
     *
     * @param serializedIngredient Serialized form of the ingredient
     * @return A completable future with an optionally present ingredient
     * @throws IllegalArgumentException if the ingredient meta in the string was invalid
     */
    default CompletableFuture<Optional<Ingredient>> deserializeIngredient(@NotNull String serializedIngredient) throws IllegalArgumentException {
        Matcher matcher = INGREDIENT_META_DATA_RE.matcher(serializedIngredient);
        if (!matcher.find()) {
            return getIngredient(serializedIngredient);
        }
        String meta = matcher.group(1);
        String id = matcher.replaceAll("");
        if(meta.isBlank()) {
            return getIngredient(id);
        }
        ImmutableMap.Builder<IngredientMeta<?>, Object> metaBuilder = new ImmutableMap.Builder<>();
        StringBuilder metaElementBuilder = new StringBuilder();
        boolean inQuotes = false;
        boolean escaping = false;
        int curlyBracketsDepth = 0;
        for (char character : meta.toCharArray()) {
            if (character == '\\' && inQuotes) {
                escaping = true;
                continue;
            }
            if (!escaping && character == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (!inQuotes && character == '{') {
                curlyBracketsDepth++;
            }
            if (!inQuotes && character == '}') {
                curlyBracketsDepth--;
            }
            if (curlyBracketsDepth < 0) {
                throw new IllegalArgumentException("Invalid syntax, expected leading curly brace");
            }
            escaping = false;
            if(!inQuotes && curlyBracketsDepth == 0 && character == ',') {
                String metaElement = metaElementBuilder.toString();
                addMeta(metaElement, metaBuilder);
                metaElementBuilder = new StringBuilder();
                continue;
            }
            metaElementBuilder.append(character);
        }
        if(!metaElementBuilder.isEmpty()) {
            addMeta(metaElementBuilder.toString(), metaBuilder);
        }
        return getIngredient(id)
                .thenApply(ingredientOptional -> ingredientOptional
                        .map(ingredient -> new IngredientWithMeta(ingredient, metaBuilder.build()))
                );
    }

    private void addMeta(String metaElement, ImmutableMap.Builder<IngredientMeta<?>, Object> metaBuilder){
        Matcher elementMatcher = INGREDIENT_META_DATA_ELEMENT_RE.matcher(metaElement);
        Preconditions.checkArgument(elementMatcher.matches(), "Invalid ingredient meta pattern: " + metaElement);
        String key = elementMatcher.group(1).strip();
        String value = elementMatcher.group(2).strip();
        IngredientMeta<?> ingredientMeta = BreweryRegistry.INGREDIENT_META.get(BreweryKey.parse(key));
        Preconditions.checkArgument(ingredientMeta != null, "Invalid ingredient meta, unknown key: " + key);
        Object deserialized = ingredientMeta.serializer().deserialize(value);
        metaBuilder.put(ingredientMeta, deserialized);
    }

    /**
     * Serialize the ingredient with meta data. Meta data is within curly brackets
     *
     * @param ingredient Ingredient to serialize
     * @return Ingredient in serialized form
     * @throws IllegalArgumentException If the ingredient contains invalid meta data
     */
    default String serializeIngredient(Ingredient ingredient) throws IllegalArgumentException {
        if (!(ingredient instanceof IngredientWithMeta metaIngredient)) {
            return ingredient.getKey();
        }
        Map<IngredientMeta<?>, Object> meta = metaIngredient.meta();
        return String.format("%s{%s}", metaIngredient.getKey(),
                meta.entrySet()
                        .stream()
                        .map(entry -> entry.getKey().key().minimalized() + "=" + entry.getKey().serializer().serializeSafely(entry.getValue()))
                        .collect(Collectors.joining(","))
        );
    }

    /**
     * @param ingredientStr A string with the format [ingredient-name]/[runs]. Allows not specifying runs, where it will default to 1
     * @return An ingredient/runs pair
     * @throws IllegalArgumentException if the ingredients string is invalid
     */
    CompletableFuture<Pair<Ingredient, Integer>> getIngredientWithAmount(String ingredientStr) throws IllegalArgumentException;

    CompletableFuture<Pair<Ingredient, Integer>> getIngredientWithAmount(String ingredientStr, boolean withMeta) throws
            IllegalArgumentException;

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


    /**
     * Parse a list of strings into a map of ingredients with runs
     *
     * @param stringList A list of strings with valid formatting, see {@link #getIngredientWithAmount(String)}
     * @param withMeta   True if meta parsing is allowed
     * @return A map representing ingredients with runs
     * @throws IllegalArgumentException if there's any invalid ingredient string
     */
    CompletableFuture<Map<Ingredient, Integer>> getIngredientsWithAmount(List<String> stringList, boolean withMeta) throws
            IllegalArgumentException;
}
