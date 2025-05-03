package dev.jsinco.brewery.brew;

import com.google.gson.JsonArray;
import dev.jsinco.brewery.ingredient.IngredientManager;
import dev.jsinco.brewery.util.FutureUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BrewSerializer {

    public static final BrewSerializer INSTANCE = new BrewSerializer();

    public JsonArray serialize(Brew brew) {
        JsonArray array = new JsonArray();
        for (BrewingStep step : brew.getCompletedSteps()) {
            array.add(BrewingStepSerializer.INSTANCE.serialize(step));
        }
        return array;
    }

    public CompletableFuture<Brew> deserialize(JsonArray jsonArray, IngredientManager<?> ingredientManager) {
        List<CompletableFuture<BrewingStep>> brewingStepFutures = jsonArray.asList().stream()
                .map(jsonElement -> BrewingStepSerializer.INSTANCE.deserialize(jsonElement, ingredientManager))
                .toList();
        return FutureUtil.mergeFutures(brewingStepFutures)
                .thenApplyAsync(BrewImpl::new);
    }
}
