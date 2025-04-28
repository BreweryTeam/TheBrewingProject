package dev.jsinco.brewery.ingredient;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.jsinco.brewery.util.Logging;

import java.util.Map;

public class IngredientUtil {

    private IngredientUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<Ingredient, Integer> ingredientsFromJson(JsonObject json, IngredientManager<?> ingredientManager) {
        ImmutableMap.Builder<Ingredient, Integer> output = new ImmutableMap.Builder<>();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            ingredientManager.getIngredient(entry.getKey())
                    .ifPresentOrElse(ingredient -> output.put(ingredient, entry.getValue().getAsInt()),
                            () -> Logging.warning("Could not find ingredient for stored brew: " + entry.getKey()));
        }
        return output.build();
    }

    public static JsonObject ingredientsToJson(Map<Ingredient, Integer> ingredients) {
        JsonObject output = new JsonObject();
        for (Map.Entry<Ingredient, Integer> entry : ingredients.entrySet()) {
            output.add(entry.getKey().getKey(), new JsonPrimitive(entry.getValue()));
        }
        return output;
    }
}
