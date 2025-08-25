package dev.jsinco.brewery.brew;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.ingredient.IngredientManager;
import dev.jsinco.brewery.ingredient.IngredientUtil;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.BreweryRegistry;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BrewingStepSerializer {

    public static final BrewingStepSerializer INSTANCE = new BrewingStepSerializer();

    public JsonObject serialize(BrewingStep step) {
        JsonObject object = new JsonObject();
        object.addProperty("type", step.stepType().name().toLowerCase(Locale.ROOT));
        switch (step) {
            case AgeStepImpl(Moment age, BarrelType type) -> {
                object.add("age", Moment.SERIALIZER.serialize(age));
                object.addProperty("barrel_type", type.key().toString());
            }
            case CookStepImpl(
                    Moment brewTime, Map<? extends Ingredient, Integer> ingredients,
                    CauldronType cauldronType
            ) -> {
                object.add("brew_time", Moment.SERIALIZER.serialize(brewTime));
                object.addProperty("cauldron_type", cauldronType.key().toString());
                object.add("ingredients", IngredientUtil.ingredientsToJson((Map<Ingredient, Integer>) ingredients));
            }
            case DistillStepImpl(int runs) -> {
                object.addProperty("runs", runs);
            }
            case MixStepImpl(Moment time, Map<? extends Ingredient, Integer> ingredients) -> {
                object.add("ingredients", IngredientUtil.ingredientsToJson((Map<Ingredient, Integer>) ingredients));
                object.add("mix_time", Moment.SERIALIZER.serialize(time));
            }
            default -> throw new IllegalStateException("Unexpected value: " + step);
        }
        return object;
    }

    public CompletableFuture<BrewingStep> deserialize(JsonElement jsonElement, IngredientManager<?> ingredientManager) {
        JsonObject object = jsonElement.getAsJsonObject();
        BrewingStep.StepType stepType = BrewingStep.StepType.valueOf(object.get("type").getAsString().toUpperCase(Locale.ROOT));
        return switch (stepType) {
            case COOK ->
                    IngredientUtil.ingredientsFromJson(object.get("ingredients").getAsJsonObject(), ingredientManager)
                            .thenApplyAsync(ingredients -> new CookStepImpl(
                                    Moment.SERIALIZER.deserialize(object.get("brew_time")),
                                    ingredients,
                                    BreweryRegistry.CAULDRON_TYPE.get(BreweryKey.parse(object.get("cauldron_type").getAsString()))
                            ));
            case DISTILL -> CompletableFuture.completedFuture(new DistillStepImpl(object.get("runs").getAsInt()));
            case AGE ->
                    CompletableFuture.completedFuture(new AgeStepImpl(Moment.SERIALIZER.deserialize(object.get("age")), BreweryRegistry.BARREL_TYPE.get(BreweryKey.parse(object.get("barrel_type").getAsString()))));
            case MIX ->
                    IngredientUtil.ingredientsFromJson(object.get("ingredients").getAsJsonObject(), ingredientManager)
                            .thenApplyAsync(ingredients -> new MixStepImpl(
                                    Moment.SERIALIZER.deserialize(object.get("mix_time")),
                                    ingredients
                            ));
        };
    }
}
