package dev.jsinco.brewery.ingredient;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.util.FutureUtil;
import dev.jsinco.brewery.api.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class IngredientUtil {

    private IngredientUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static CompletableFuture<Map<Ingredient, Integer>> ingredientsFromJson(JsonObject json, IngredientManager<?> ingredientManager) {

        List<CompletableFuture<Pair<Ingredient, Integer>>> ingredientsFuture = json.entrySet()
                .stream()
                .map(jsonEntry -> ingredientManager.getIngredient(jsonEntry.getKey())
                        .thenApplyAsync(optionalIngredient -> optionalIngredient
                                .map(ingredient -> new Pair<>(ingredient, jsonEntry.getValue().getAsInt()))
                                .orElseThrow(() -> new IllegalArgumentException(jsonEntry.getKey() + " is not a valid ingredient"))
                        )
                ).toList();
        return FutureUtil.mergeFutures(ingredientsFuture)
                .thenApplyAsync(ingredientPairs -> {
                    ImmutableMap.Builder<Ingredient, Integer> output = new ImmutableMap.Builder<>();
                    ingredientPairs.forEach(ingredientPair -> output.put(ingredientPair.first(), ingredientPair.second()));
                    return output.build();
                });
    }

    public static JsonObject ingredientsToJson(Map<Ingredient, Integer> ingredients) {
        JsonObject output = new JsonObject();
        for (Map.Entry<Ingredient, Integer> entry : ingredients.entrySet()) {
            output.add(entry.getKey().getKey(), new JsonPrimitive(entry.getValue()));
        }
        return output;
    }
}
