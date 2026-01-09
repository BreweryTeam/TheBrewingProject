package dev.jsinco.brewery.brew;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.meta.MetaSerializer;
import dev.jsinco.brewery.util.FutureUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BrewSerializer {

    public static final BrewSerializer INSTANCE = new BrewSerializer();
    private static final int VERSION = 1;

    public JsonElement serialize(Brew brew) {
        JsonObject obj = new JsonObject();
        obj.addProperty("version", VERSION);
        obj.add("steps", steps(brew));
        obj.add("meta", MetaSerializer.INSTANCE.serialize(brew.meta()));
        return obj;
    }

    private static JsonArray steps(Brew brew) {
        JsonArray array = new JsonArray();
        for (BrewingStep step : brew.getSteps()) {
            array.add(BrewingStepSerializer.INSTANCE.serialize(step));
        }
        return array;
    }

    public CompletableFuture<Brew> deserialize(JsonElement jsonElement, IngredientManager<?> ingredientManager) {
        if (jsonElement.isJsonArray()) {
            return deserializeVersion0(jsonElement, ingredientManager);
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        int version = jsonObject.has("version") ? 0 : jsonObject.get("version").getAsInt();
        if (version < 1 || version > VERSION) {
            throw new RuntimeException("Unsupported version: " + version);
        }
        return getSteps(jsonObject.getAsJsonArray("steps"), ingredientManager)
                .thenApplyAsync(steps -> new BrewImpl(
                        steps, getMeta(jsonObject.getAsJsonObject("meta"))
                ));
    }

    private static CompletableFuture<Brew> deserializeVersion0(JsonElement jsonElement, IngredientManager<?> ingredientManager) {
        return getSteps(jsonElement.getAsJsonArray(), ingredientManager)
                .thenApplyAsync(BrewImpl::new);
    }

    private static CompletableFuture<List<BrewingStep>> getSteps(@Nullable JsonArray jsonArray, IngredientManager<?> ingredientManager) {
        if (jsonArray == null) {
            return CompletableFuture.completedFuture(List.of());
        }
        List<CompletableFuture<BrewingStep>> brewingStepFutures = jsonArray.asList().stream()
                .map(element -> BrewingStepSerializer.INSTANCE.deserialize(element, ingredientManager))
                .toList();
        return FutureUtil.mergeFutures(brewingStepFutures);
    }

    private static MetaData getMeta(@Nullable JsonObject jsonObject) {
        if (jsonObject == null) {
            return new MetaData();
        }
        return MetaSerializer.INSTANCE.deserialize(jsonObject);
    }
}
