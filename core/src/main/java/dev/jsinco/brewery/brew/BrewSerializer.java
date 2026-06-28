package dev.jsinco.brewery.brew;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.ResolvedIngredientManager;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.meta.MetaSerializer;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BrewSerializer {

    public static final BrewSerializer INSTANCE = new BrewSerializer();
    private static final int VERSION = 1;

    public JsonElement serialize(Brew brew, ResolvedIngredientManager<?> ingredientManager) {
        JsonObject obj = new JsonObject();
        obj.addProperty("version", VERSION);
        obj.add("steps", steps(brew, ingredientManager));
        obj.add("meta", MetaSerializer.INSTANCE.serialize(brew.meta()));
        return obj;
    }

    private static JsonArray steps(Brew brew, ResolvedIngredientManager<?> ingredientManager) {
        JsonArray array = new JsonArray();
        for (BrewingStep step : brew.getSteps()) {
            array.add(BrewingStepSerializer.INSTANCE.serialize(step, ingredientManager));
        }
        return array;
    }

    public Brew deserialize(JsonElement jsonElement, ResolvedIngredientManager<?> ingredientManager) {
        if (jsonElement.isJsonArray()) {
            return deserializeVersion0(jsonElement, ingredientManager);
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        int version = jsonObject.has("version") ? jsonObject.get("version").getAsInt() : 0;
        if (version < 1 || version > VERSION) {
            throw new RuntimeException("Unsupported version: " + version);
        }
        return new BrewImpl(
                getSteps(jsonObject.getAsJsonArray("steps"), ingredientManager),
                getMeta(jsonObject.getAsJsonObject("meta"))
        );
    }

    private static Brew deserializeVersion0(JsonElement jsonElement, ResolvedIngredientManager<?> ingredientManager) {
        return new BrewImpl(getSteps(jsonElement.getAsJsonArray(), ingredientManager));
    }

    private static List<BrewingStep> getSteps(@Nullable JsonArray jsonArray, ResolvedIngredientManager<?> ingredientManager) {
        if (jsonArray == null) {
            return List.of();
        }
        return jsonArray.asList().stream()
                .map(element -> BrewingStepSerializer.INSTANCE.deserialize(element, ingredientManager))
                .toList();
    }

    private static MetaData getMeta(@Nullable JsonObject jsonObject) {
        if (jsonObject == null) {
            return new MetaData();
        }
        return MetaSerializer.INSTANCE.deserialize(jsonObject);
    }
}
