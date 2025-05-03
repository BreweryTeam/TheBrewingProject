package dev.jsinco.brewery.ingredient;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.jsinco.brewery.util.Logging;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class IngredientUtil {

    private IngredientUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static CompletableFuture<Map<Ingredient, Integer>> ingredientsFromJson(JsonObject json, IngredientManager<?> ingredientManager) {
        ImmutableMap.Builder<Ingredient, Integer> output = new ImmutableMap.Builder<>();
        CompletableFuture<?>[] ingredientsFuture = json.entrySet()
                .stream()
                .map(jsonEntry -> ingredientManager.getIngredient(jsonEntry.getKey())
                        .thenAcceptAsync(optionalIngredient ->
                                optionalIngredient.ifPresentOrElse(ingredient -> output.put(ingredient, jsonEntry.getValue().getAsInt()),
                                        () -> Logging.warning("Could not find ingredient for stored brew: " + jsonEntry.getKey()))))
                .toArray(CompletableFuture<?>[]::new);
        return CompletableFuture.allOf(ingredientsFuture).thenApplyAsync(ignored -> output.build());
    }

    public static JsonObject ingredientsToJson(Map<Ingredient, Integer> ingredients) {
        JsonObject output = new JsonObject();
        for (Map.Entry<Ingredient, Integer> entry : ingredients.entrySet()) {
            output.add(entry.getKey().getKey(), new JsonPrimitive(entry.getValue()));
        }
        return output;
    }
}
