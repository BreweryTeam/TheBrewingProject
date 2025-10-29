package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.brew.MixStepImpl;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.util.FutureUtil;
import dev.jsinco.brewery.time.TimeUtil;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RecipeReader<I> {

    private final File folder;
    private final RecipeResultReader<I> recipeResultReader;
    private final IngredientManager<I> ingredientManager;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public RecipeReader(File folder, RecipeResultReader<I> recipeResultReader, IngredientManager<I> ingredientManager) {
        this.folder = folder;
        this.recipeResultReader = recipeResultReader;
        this.ingredientManager = ingredientManager;
    }

    public List<CompletableFuture<RecipeImpl<I>>> readRecipes() {
        Path mainDir = folder.toPath();
        YamlFile recipesFile = new YamlFile(mainDir.resolve("recipes.yml").toFile());

        try {
            recipesFile.createOrLoadWithComments();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConfigurationSection recipesSection = recipesFile.getConfigurationSection("recipes");
        return recipesSection.getKeys(false)
                .stream()
                .map(key -> getRecipe(recipesSection.getConfigurationSection(key), key).handleAsync((recipe, exception) -> {
                            if (exception != null) {
                                Logger.logErr("Exception when reading recipe: " + key);
                                Logger.logErr(exception);
                                return null;
                            }
                            return recipe;
                        }, executor) // Single thread executor to make reading stacktraces possible
                )
                .toList();
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
                            new PassedMoment(TimeUtil.parse(map.get("cook-time").toString(), TimeUtil.TimeUnit.COOKING_MINUTES)),
                            ingredients,
                            BreweryRegistry.CAULDRON_TYPE.get(BreweryKey.parse(map.containsKey("cauldron-type") ? map.get("cauldron-type").toString().toLowerCase(Locale.ROOT) : "water"))
                    ));
            case DISTILL -> CompletableFuture.completedFuture(new DistillStepImpl(
                    (int) map.get("runs")
            ));
            case AGE -> CompletableFuture.completedFuture(new AgeStepImpl(
                    new PassedMoment(TimeUtil.parse(map.get("age-years").toString(), TimeUtil.TimeUnit.AGING_YEARS)),
                    BreweryRegistry.BARREL_TYPE.get(BreweryKey.parse(map.get("barrel-type").toString()))
            ));
            case MIX -> ingredientManager.getIngredientsWithAmount((List<String>) map.get("ingredients"))
                    .thenApplyAsync(ingredients -> new MixStepImpl(
                            new PassedMoment(TimeUtil.parse(map.get("mix-time").toString(), TimeUtil.TimeUnit.COOKING_MINUTES)),
                            ingredients
                    ));
        };
    }

    private void checkStep(BrewingStep.StepType type, Map<?, ?> map) throws IllegalArgumentException {
        switch (type) {
            case COOK -> {
                Preconditions.checkArgument(map.containsKey("cook-time") && TimeUtil.validTime(map.get("cook-time").toString()), "Expected a number, or a time format for 'cook-time' in cooking step!");
                Preconditions.checkArgument(map.get("ingredients") instanceof List, "Expected string list value for 'ingredients' in cook step!");
                Preconditions.checkArgument(!map.containsKey("cauldron-type") || map.get("cauldron-type") instanceof String, "Expected string value for 'cauldron-type' in cook step!");
                String cauldronType = map.containsKey("cauldron-type") ? (String) map.get("cauldron-type") : "water";
                Preconditions.checkArgument(BreweryRegistry.CAULDRON_TYPE.containsKey(BreweryKey.parse(cauldronType)), "Expected a valid cauldron type for 'cauldron-type' in cook step!");
            }
            case DISTILL ->
                    Preconditions.checkArgument(map.get("runs") instanceof Integer integer && integer > 0, "Expected a positive integer value for 'runs' in distill step!");
            case AGE -> {
                Preconditions.checkArgument(map.containsKey("age-years") && TimeUtil.parse(map.get("age-years").toString(), TimeUtil.TimeUnit.AGING_YEARS) > Config.config().barrels().agingYearTicks() / 2, "Expected a time longer than half an aging year for 'age-years' in age step!");
                Preconditions.checkArgument(!map.containsKey("barrel-type") || map.get("barrel-type") instanceof String, "Expected string value for 'barrel-type' in age step!");
                String barrelType = map.containsKey("barrel-type") ? (String) map.get("barrel-type") : "any";
                Preconditions.checkArgument(BreweryRegistry.BARREL_TYPE.containsKey(BreweryKey.parse(barrelType)), "Expected a valid barrel type for 'barrel-type' in age step!");
            }
            case MIX -> {
                Preconditions.checkArgument(map.containsKey("mix-time") && TimeUtil.validTime(map.get("mix-time").toString()), "Expected a number, or a time format for 'mix-time' in mix step!");
                Preconditions.checkArgument(map.get("ingredients") instanceof List, "Expected string list value for 'ingredients' in mix step!");
            }
        }
    }
}