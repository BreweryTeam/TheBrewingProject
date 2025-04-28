package dev.jsinco.brewery.brew;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.ingredient.IngredientManager;
import dev.jsinco.brewery.ingredient.IngredientUtil;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Registry;
import dev.jsinco.brewery.moment.Moment;

import java.util.Locale;
import java.util.Map;

public class BrewingStepSerializer {

    public static final BrewingStepSerializer INSTANCE = new BrewingStepSerializer();

    public JsonObject serialize(BrewingStep step) {
        JsonObject object = new JsonObject();
        object.addProperty("type", step.stepType().name().toLowerCase(Locale.ROOT));
        switch (step) {
            case BrewingStep.Age(Moment age, BarrelType type) -> {
                object.add("age", Moment.SERIALIZER.serialize(age));
                object.addProperty("barrel_type", type.key().toString());
            }
            case BrewingStep.Cook(
                    Moment brewTime, Map<? extends Ingredient, Integer> ingredients,
                    CauldronType cauldronType
            ) -> {
                object.add("brew_time", Moment.SERIALIZER.serialize(brewTime));
                object.addProperty("cauldron_type", cauldronType.key().toString());
                object.add("ingredients", IngredientUtil.ingredientsToJson((Map<Ingredient, Integer>) ingredients));
            }
            case BrewingStep.Distill(int runs) -> {
                object.addProperty("runs", runs);
            }
            case BrewingStep.Mix(Moment time, Map<? extends Ingredient, Integer> ingredients) -> {
                object.add("ingredients", IngredientUtil.ingredientsToJson((Map<Ingredient, Integer>) ingredients));
                object.add("mix_time", Moment.SERIALIZER.serialize(time));
            }
        }
        return object;
    }

    public BrewingStep deserialize(JsonElement jsonElement, IngredientManager<?> ingredientManager) {
        JsonObject object = jsonElement.getAsJsonObject();
        BrewingStep.StepType stepType = BrewingStep.StepType.valueOf(object.get("type").getAsString().toUpperCase(Locale.ROOT));
        return switch (stepType) {
            case COOK -> new BrewingStep.Cook(
                    Moment.SERIALIZER.deserialize(object.get("brew_time")),
                    IngredientUtil.ingredientsFromJson(object.get("ingredients").getAsJsonObject(), ingredientManager),
                    Registry.CAULDRON_TYPE.get(BreweryKey.parse(object.get("cauldron_type").getAsString()))
            );
            case DISTILL -> new BrewingStep.Distill(object.get("runs").getAsInt());
            case AGE ->
                    new BrewingStep.Age(Moment.SERIALIZER.deserialize(object.get("age")), Registry.BARREL_TYPE.get(BreweryKey.parse(object.get("barrel_type").getAsString())));
            case MIX -> new BrewingStep.Mix(
                    Moment.SERIALIZER.deserialize(object.get("mix_time")),
                    IngredientUtil.ingredientsFromJson(object.get("ingredients").getAsJsonObject(), ingredientManager)
            );
        };
    }
}
