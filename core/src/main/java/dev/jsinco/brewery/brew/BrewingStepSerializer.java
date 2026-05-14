package dev.jsinco.brewery.brew;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.ingredient.IngredientUtil;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.SequencedSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BrewingStepSerializer {

    public static final BrewingStepSerializer INSTANCE = new BrewingStepSerializer();

    public JsonObject serialize(BrewingStep step, IngredientManager<?> ingredientManager) {
        JsonObject object = new JsonObject();
        object.addProperty("type", step.stepType().name().toLowerCase(Locale.ROOT));
        switch (step) {
            case AgeStepImpl(Moment age, BarrelType type, SequencedSet<UUID> brewers, int mergeCount) -> {
                object.add("age", Moment.SERIALIZER.serialize(age));
                object.addProperty("barrel_type", type.key().toString());
                if (!brewers.isEmpty()) {
                    object.add("brewers", brewersToJson(brewers));
                }
                if (mergeCount != 1) {
                    object.addProperty("merge_count", mergeCount);
                }
            }
            case CookStepImpl(
                    Moment brewTime, Map<? extends Ingredient, Integer> ingredients,
                    CauldronType cauldronType,
                    SequencedSet<UUID> brewers,
                    int mergeCount
            ) -> {
                object.add("brew_time", Moment.SERIALIZER.serialize(brewTime));
                if (cauldronType != null) {
                    object.addProperty("cauldron_type", cauldronType.key().toString());
                }
                object.add("ingredients", IngredientUtil.ingredientsToJson((Map<Ingredient, Integer>) ingredients, ingredientManager));
                if (!brewers.isEmpty()) {
                    object.add("brewers", brewersToJson(brewers));
                }
                if (mergeCount != 1) {
                    object.addProperty("merge_count", mergeCount);
                }
            }
            case DistillStepImpl(int runs, SequencedSet<UUID> brewers, int mergeCount) -> {
                object.addProperty("runs", runs);
                if (!brewers.isEmpty()) {
                    object.add("brewers", brewersToJson(brewers));
                }
                if (mergeCount != 1) {
                    object.addProperty("merge_count", mergeCount);
                }
            }
            case MixStepImpl(
                    Moment time, Map<? extends Ingredient, Integer> ingredients, CauldronType cauldronType,
                    SequencedSet<UUID> brewers, int mergeCount
            ) -> {
                object.add("ingredients", IngredientUtil.ingredientsToJson((Map<Ingredient, Integer>) ingredients, ingredientManager));
                object.add("mix_time", Moment.SERIALIZER.serialize(time));
                if (cauldronType != null) {
                    object.addProperty("cauldron_type", cauldronType.key().toString());
                }
                if (!brewers.isEmpty()) {
                    object.add("brewers", brewersToJson(brewers));
                }
                if (mergeCount != 1) {
                    object.addProperty("merge_count", mergeCount);
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
                                    object.has("cauldron_type")
                                            ? BreweryRegistry.CAULDRON_TYPE.get(BreweryKey.parse(object.get("cauldron_type").getAsString()))
                                            : null,
                                    jsonToBrewers(object),
                                    object.has("merge_count") ? object.get("merge_count").getAsInt() : 1
                            ));
            case DISTILL -> CompletableFuture.completedFuture(new DistillStepImpl(
                    object.get("runs").getAsInt(),
                    jsonToBrewers(object),
                    object.has("merge_count") ? object.get("merge_count").getAsInt() : 1
            ));
            case AGE -> CompletableFuture.completedFuture(new AgeStepImpl(
                    Moment.SERIALIZER.deserialize(object.get("age")),
                    BreweryRegistry.BARREL_TYPE.get(BreweryKey.parse(object.get("barrel_type").getAsString())),
                    jsonToBrewers(object),
                    object.has("merge_count") ? object.get("merge_count").getAsInt() : 1
            ));
            case MIX ->
                    IngredientUtil.ingredientsFromJson(object.get("ingredients").getAsJsonObject(), ingredientManager)
                            .thenApplyAsync(ingredients -> new MixStepImpl(
                                    Moment.SERIALIZER.deserialize(object.get("mix_time")),
                                    ingredients,
                                    object.has("cauldron_type")
                                            ? BreweryRegistry.CAULDRON_TYPE.get(BreweryKey.parse(object.get("cauldron_type").getAsString()))
                                            : null,
                                    jsonToBrewers(object),
                                    object.has("merge_count") ? object.get("merge_count").getAsInt() : 1
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