package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.brew.*;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.ingredient.IngredientManager;
import dev.jsinco.brewery.moment.PassedMoment;
import dev.jsinco.brewery.recipe.Recipe;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.FutureUtil;
import dev.jsinco.brewery.util.Logging;
import dev.jsinco.brewery.util.Registry;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RecipeReader<I> {

    private final File folder;
    private final RecipeResultReader<I> recipeResultReader;
    private final IngredientManager<I> ingredientManager;

    public RecipeReader(File folder, RecipeResultReader<I> recipeResultReader, IngredientManager<I> ingredientManager) {
        this.folder = folder;
        this.recipeResultReader = recipeResultReader;
        this.ingredientManager = ingredientManager;
    }

    public CompletableFuture<Map<String, Recipe<I>>> readRecipes() {
        Path mainDir = folder.toPath();
        YamlFile recipesFile = new YamlFile(mainDir.resolve("recipes.yml").toFile());

        try {
            recipesFile.createOrLoadWithComments();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConfigurationSection recipesSection = recipesFile.getConfigurationSection("recipes");
        List<CompletableFuture<RecipeImpl<I>>> futures = recipesSection.getKeys(false)
                .stream()
                .map(key -> getRecipe(recipesSection.getConfigurationSection(key), key).handleAsync((recipe, exception) -> {
                    if (exception != null) {
                        Logging.error("Exception when reading recipe: " + key);
                        if (exception.getCause() != null) {
                            exception.getCause().printStackTrace();
                        } else {
                            exception.printStackTrace();
                        }
                        return null;
                    }
                    return recipe;
                }))
                .toList();
        return FutureUtil.mergeFutures(futures)
                .thenApplyAsync(recipes -> {
                    ImmutableMap.Builder<String, Recipe<I>> recipesMap = new ImmutableMap.Builder<>();
                    recipes.stream()
                            .filter(Objects::nonNull)
                            .forEach(recipe -> recipesMap.put(recipe.getRecipeName(), recipe));
                    return recipesMap.build();
                });
    }

    /**
     * Obtain a recipe from the recipes.yml file.
     *
     * @param recipeName The name/id of the recipe to obtain. Ex: 'example_recipe'
     * @return A Recipe object with all the attributes of the recipe.
     */
    private CompletableFuture<RecipeImpl<I>> getRecipe(ConfigurationSection recipe, String recipeName) {
        try {
            return parseSteps(recipe.getMapList("steps")).thenApplyAsync(steps -> new RecipeImpl.Builder<I>(recipeName)
                    .brewDifficulty(recipe.getDouble("brew-difficulty", 1D))
                    .recipeResults(recipeResultReader.readRecipeResults(recipe))
                    .steps(steps)
                    .build()
            );
        } catch (Throwable e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private @NotNull CompletableFuture<List<BrewingStep>> parseSteps(List<Map<?, ?>> steps) {
        return FutureUtil.mergeFutures(steps.stream()
                .map(this::parseStep)
                .toList());
    }

    private CompletableFuture<BrewingStep> parseStep(Map<?, ?> map) {
        BrewingStep.StepType type = BrewingStep.StepType.valueOf(String.valueOf(map.get("type")).toUpperCase(Locale.ROOT));
        checkStep(type, map);
        return switch (type) {
            case COOK -> ingredientManager.getIngredientsWithAmount((List<String>) map.get("ingredients"))
                    .thenApplyAsync(ingredients -> new CookStepImpl(
                            new PassedMoment((long) ((Double) map.get("cook-time") * Config.COOKING_MINUTE_TICKS)),
                            ingredients,
                            Registry.CAULDRON_TYPE.get(BreweryKey.parse(map.containsKey("cauldron-type") ? map.get("cauldron-type").toString().toLowerCase(Locale.ROOT) : "water"))
                    ));
            case DISTILL -> CompletableFuture.completedFuture(new DistillStepImpl(
                    (int) map.get("runs")
            ));
            case AGE -> CompletableFuture.completedFuture(new AgeStepImpl(
                    new PassedMoment((long) ((Double) map.get("age-years") * Config.AGING_YEAR_TICKS)),
                    Registry.BARREL_TYPE.get(BreweryKey.parse(map.get("barrel-type").toString().toLowerCase(Locale.ROOT)))
            ));
            case MIX -> ingredientManager.getIngredientsWithAmount((List<String>) map.get("ingredients"))
                    .thenApplyAsync(ingredients -> new MixStepImpl(
                            new PassedMoment((long) ((Double) map.get("mix-time") * Config.COOKING_MINUTE_TICKS)),
                            ingredients
                    ));
        };
    }

    private void checkStep(BrewingStep.StepType type, Map<?, ?> map) throws IllegalArgumentException {
        switch (type) {
            case COOK -> {
                Preconditions.checkArgument(map.get("cook-time") instanceof Double doubleValue && doubleValue > 0, "Expected positive number value for 'cook-time' in cook step!");
                Preconditions.checkArgument(map.get("ingredients") instanceof List, "Expected string list value for 'ingredients' in cook step!");
                Preconditions.checkArgument(!map.containsKey("cauldron-type") || map.get("cauldron-type") instanceof String, "Expected string value for 'cauldron-type' in cook step!");
                String cauldronType = map.containsKey("cauldron-type") ? (String) map.get("cauldron-type") : "water";
                Preconditions.checkArgument(Registry.CAULDRON_TYPE.containsKey(BreweryKey.parse(cauldronType)), "Expected a valid cauldron type for 'cauldron-type' in cook step!");
            }
            case DISTILL ->
                    Preconditions.checkArgument(map.get("runs") instanceof Integer, "Expected integer value for 'runs' in distill step!");
            case AGE -> {
                Preconditions.checkArgument(map.get("age-years") instanceof Double doubleValue && doubleValue > 0, "Expected positive number value for 'age-years' in age step!");
                Preconditions.checkArgument(!map.containsKey("barrel-type") || map.get("barrel-type") instanceof String, "Expected string value for 'barrel-type' in age step!");
                String barrelType = map.containsKey("barrel-type") ? (String) map.get("barrel-type") : "any";
                Preconditions.checkArgument(Registry.BARREL_TYPE.containsKey(BreweryKey.parse(barrelType)), "Expected a valid barrel type for 'barrel-type' in age step!");
            }
            case MIX -> {
                Preconditions.checkArgument(map.get("mix-time") instanceof Double doubleValue && doubleValue > 0, "Expected positive number value for 'mix-time' in mix step!");
                Preconditions.checkArgument(map.get("ingredients") instanceof List, "Expected string list value for 'ingredients' in mix step!");
            }
        }
    }

    public static int parseAlcoholString(String str) {
        return Integer.parseInt(str.replace("%", "").replace(" ", ""));
    }
}