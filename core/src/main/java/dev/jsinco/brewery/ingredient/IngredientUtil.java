package dev.jsinco.brewery.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.ResolvedIngredientManager;
import dev.jsinco.brewery.util.MapUtil;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class IngredientUtil {

    private IngredientUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<Ingredient, Integer> ingredientsFromJson(JsonObject json, ResolvedIngredientManager<?> ingredientManager) {
        return json.entrySet()
                .stream()
                .map(MapUtil.mapKey(ingredientManager::deserializeIngredient))
                .map(MapUtil.mapValue(JsonElement::getAsInt))
                .map(MapUtil.optionalKey())
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static JsonObject ingredientsToJson(Map<Ingredient, Integer> ingredients, ResolvedIngredientManager<?> ingredientManager) {
        JsonObject output = new JsonObject();
        for (Map.Entry<Ingredient, Integer> entry : ingredients.entrySet()) {
            output.add(ingredientManager.serializeIngredient(entry.getKey()), new JsonPrimitive(entry.getValue()));
        }
        return output;
    }
}
