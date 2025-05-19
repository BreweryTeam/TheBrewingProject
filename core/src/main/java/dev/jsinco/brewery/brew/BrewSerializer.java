package dev.jsinco.brewery.brew;

import com.google.gson.JsonArray;
import dev.jsinco.brewery.ingredient.IngredientManager;

public class BrewSerializer {

    public static final BrewSerializer INSTANCE = new BrewSerializer();

    public JsonArray serialize(Brew brew) {
        JsonArray array = new JsonArray();
        for (BrewingStep step : brew.getCompletedSteps()) {
            array.add(BrewingStepSerializer.INSTANCE.serialize(step));
        }
        return array;
    }

    public BrewImpl deserialize(JsonArray jsonArray, IngredientManager<?> ingredientManager) {
        return new BrewImpl(jsonArray.asList().stream().map(jsonElement -> BrewingStepSerializer.INSTANCE.deserialize(jsonElement, ingredientManager)).toList());
    }
}
