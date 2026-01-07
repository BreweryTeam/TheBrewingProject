package dev.jsinco.brewery.brew;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.ingredient.IngredientUtil;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BrewingStepSerializer {

    public static final BrewingStepSerializer INSTANCE = new BrewingStepSerializer();

    public JsonObject serialize(BrewingStep step, IngredientManager<?> ingredientManager) {
        JsonObject object = new JsonObject();
        object.addProperty("type", step.stepType().name().toLowerCase(Locale.ROOT));
        switch (step) {
            case AgeStepImpl(Moment age, BarrelType type, SequencedSet<UUID> brewers) -> {
                object.add("age", Moment.SERIALIZER.serialize(age));
                object.addProperty("barrel_type", type.key().toString());
                if (!brewers.isEmpty()) {
                    object.add("brewers", brewersToJson(brewers));
                }
            }
            case CookStepImpl(
                    Moment brewTime, Map<? extends Ingredient, Integer> ingredients,
                    CauldronType cauldronType,
                    SequencedSet<UUID> brewers
            ) -> {
                object.add("brew_time", Moment.SERIALIZER.serialize(brewTime));
                object.addProperty("cauldron_type", cauldronType.key().toString());
                object.add("ingredients", IngredientUtil.ingredientsToJson((Map<Ingredient, Integer>) ingredients, ingredientManager));
                if (!brewers.isEmpty()) {
                    object.add("brewers", brewersToJson(brewers));
                }
            }
            case DistillStepImpl(int runs, SequencedSet<UUID> brewers) -> {
                object.addProperty("runs", runs);
                if (!brewers.isEmpty()) {
                    object.add("brewers", brewersToJson(brewers));
                }
            }
            case MixStepImpl(Moment time, Map<? extends Ingredient, Integer> ingredients, SequencedSet<UUID> brewers) -> {
                object.add("ingredients", IngredientUtil.ingredientsToJson((Map<Ingredient, Integer>) ingredients, ingredientManager));
                object.add("mix_time", Moment.SERIALIZER.serialize(time));
                if (!brewers.isEmpty()) {
                    object.add("brewers", brewersToJson(brewers));
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + step);
        }
        return object;
    }

    private static JsonArray brewersToJson(SequencedSet<UUID> brewers) {
        JsonArray arr = new JsonArray();
        for (UUID brewer : brewers) {
            arr.add(brewer.toString());
        }
        return arr;
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
                                    BreweryRegistry.CAULDRON_TYPE.get(BreweryKey.parse(object.get("cauldron_type").getAsString())),
                                    jsonToBrewers(object)
                            ));
            case DISTILL ->
                    CompletableFuture.completedFuture(new DistillStepImpl(
                            object.get("runs").getAsInt(),
                            jsonToBrewers(object)
                    ));
            case AGE ->
                    CompletableFuture.completedFuture(new AgeStepImpl(
                            Moment.SERIALIZER.deserialize(object.get("age")),
                            BreweryRegistry.BARREL_TYPE.get(BreweryKey.parse(object.get("barrel_type").getAsString())),
                            jsonToBrewers(object)
                    ));
            case MIX ->
                    IngredientUtil.ingredientsFromJson(object.get("ingredients").getAsJsonObject(), ingredientManager)
                            .thenApplyAsync(ingredients -> new MixStepImpl(
                                    Moment.SERIALIZER.deserialize(object.get("mix_time")),
                                    ingredients,
                                    jsonToBrewers(object)
                            ));
        };
    }

    private static SequencedSet<UUID> jsonToBrewers(JsonObject obj) {
        if (!obj.has("brewers")) {
            return Collections.emptySortedSet();
        }
        SequencedSet<UUID> brewers = new LinkedHashSet<>();
        for (JsonElement element : obj.get("brewers").getAsJsonArray()) {
            UUID brewer = UUID.fromString(element.getAsString());
            brewers.add(brewer);
        }
        return brewers;
    }
}