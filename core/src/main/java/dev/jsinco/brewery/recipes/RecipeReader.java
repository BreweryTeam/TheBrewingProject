package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.ingredient.ResolvedIngredientManager;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeGroup;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.brew.AgeStepImpl;
import dev.jsinco.brewery.brew.CookStepImpl;
import dev.jsinco.brewery.brew.DistillStepImpl;
import dev.jsinco.brewery.brew.MixStepImpl;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.time.TimeUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NonNull;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class RecipeReader<I> {

    private final File folder;
    private final RecipeResultReader<I> recipeResultReader;
    private final CompletableFuture<ResolvedIngredientManager<I>> ingredientManager;

    public RecipeReader(File folder, RecipeResultReader<I> recipeResultReader, CompletableFuture<ResolvedIngredientManager<I>> ingredientManager) {
        this.folder = folder;
        this.recipeResultReader = recipeResultReader;
        this.ingredientManager = ingredientManager;
    }

    public CompletableFuture<List<RecipeGroup<I>>> readRecipeGroups() {
        return ingredientManager.thenApply(resolvedIngredientManager -> {
            List<RecipeGroup<I>> groups = new ArrayList<>();
            readRecipeGroup(resolvedIngredientManager, new File(folder, "recipes.yml"), "main")
                    .ifPresent(groups::add);
            File[] recipesInRecipeFolder = new File(folder, "recipes").listFiles();
            if (recipesInRecipeFolder == null) {
                return groups;
            }
            for (File recipeFile : recipesInRecipeFolder) {
                if (!recipeFile.isFile() || !recipeFile.getName().endsWith(".yml")) {
                    continue;
                }
                readRecipeGroup(
                        resolvedIngredientManager,
                        recipeFile,
                        recipeFile.getName()
                                .replaceAll("\\.yml$", "")
                                .toLowerCase(Locale.ROOT)
                ).ifPresent(groups::add);
            }
            return groups;
        });
    }

    private Optional<RecipeGroup<I>> readRecipeGroup(ResolvedIngredientManager<I> resolvedIngredientManager, File path, String id) {
        YamlFile recipesFile = new YamlFile(path);

        try {
            recipesFile.createOrLoadWithComments();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!recipesFile.getBoolean("enabled", true)) {
            return Optional.empty();
        }

        ConfigurationSection recipesSection = recipesFile.getConfigurationSection("recipes");
        List<Recipe<I>> recipes = recipesSection.getKeys(false)
                .stream()
                .map(key -> getRecipe(recipesSection.getConfigurationSection(key), key, resolvedIngredientManager))
                .flatMap(Optional::stream)
                .map(recipeImpl -> (Recipe<I>) recipeImpl)
                .toList();
        String displayName = recipesFile.getString("group-display-name");
        return Optional.of(new RecipeGroupImpl<>(
                id,
                displayName == null ? null : MiniMessage.miniMessage().deserialize(displayName),
                recipes
        ));
    }

    /**
     * Obtain a recipe from the recipes.yml file.
     *
     * @param recipeName The name/id of the recipe to obtain. Ex: 'example_recipe'
     * @return A Recipe object with all the attributes of the recipe.
     */
    private Optional<RecipeImpl<I>> getRecipe(ConfigurationSection recipe, String recipeName, ResolvedIngredientManager<I> resolvedIngredientManager) {
        try {
            List<BrewingStep> steps = parseSteps(recipe.getMapList("steps"), resolvedIngredientManager);
            return Optional.of(new RecipeImpl.Builder<I>(recipeName)
                    .brewDifficulty(recipe.getDouble("brew-difficulty", 1D))
                    .recipeResults(recipeResultReader.readRecipeResults(recipe))
                    .steps(steps)
                    .build()
            );
        } catch (Throwable throwable) {
            Logger.logErr(throwable.getMessage());
            return Optional.empty();
        }
    }

    private @NonNull List<BrewingStep> parseSteps(List<Map<?, ?>> steps, ResolvedIngredientManager<I> resolvedIngredientManager) {
        List<BrewingStep> parsedSteps = new java.util.ArrayList<>();
        boolean hasParsedIngredientStep = false;
        for (Map<?, ?> step : steps) {
            BrewingStep.StepType type = BrewingStep.StepType.valueOf(
                    String.valueOf(step.get("type")).toUpperCase(Locale.ROOT)
            );
            checkStep(type, step, hasParsedIngredientStep);
            parsedSteps.add(parseStep(step, type, resolvedIngredientManager));
            hasParsedIngredientStep |= Set.of(BrewingStep.StepType.MIX, BrewingStep.StepType.COOK)
                    .contains(type);
        }
        return parsedSteps;
    }

    private BrewingStep parseStep(Map<?, ?> map, BrewingStep.StepType type, ResolvedIngredientManager<I> resolvedIngredientManager) {

        return switch (type) {
            case COOK -> {
                List<String> ingredientList = map.containsKey("ingredients")
                        ? (List<String>) map.get("ingredients") : List.of();
                CauldronType cauldronType = map.containsKey("cauldron-type") ? BreweryRegistry.CAULDRON_TYPE.get(
                        BreweryKey.parse(map.get("cauldron-type").toString().toLowerCase(Locale.ROOT))
                ) : null;
                yield new CookStepImpl(
                        parseTime(map, TimeUtil.TimeUnit.COOKING_MINUTES, "time", "cook-time"),
                        resolvedIngredientManager.getIngredientsWithAmount(ingredientList),
                        cauldronType
                );
            }
            case DISTILL -> new DistillStepImpl(
                    (int) map.get("runs")
            );
            case AGE -> new AgeStepImpl(
                    parseTime(map, TimeUtil.TimeUnit.AGING_YEARS, "age-years", "time"),
                    BreweryRegistry.BARREL_TYPE.get(BreweryKey.parse(map.get("barrel-type").toString()))
            );
            case MIX -> {
                List<String> ingredientList = map.containsKey("ingredients")
                        ? (List<String>) map.get("ingredients") : List.of();
                CauldronType cauldronType = map.containsKey("cauldron-type") ? BreweryRegistry.CAULDRON_TYPE.get(
                        BreweryKey.parse(map.get("cauldron-type").toString().toLowerCase(Locale.ROOT))
                ) : null;
                yield new MixStepImpl(
                        parseTime(map, TimeUtil.TimeUnit.COOKING_MINUTES, "mix-time", "time"),
                        resolvedIngredientManager.getIngredientsWithAmount(ingredientList),
                        cauldronType
                );
            }
        };
    }

    private PassedMoment parseTime(Map<?, ?> map, TimeUtil.TimeUnit timeUnit, String... aliases) {
        for (String alias : aliases) {
            if (map.containsKey(alias)) {
                return new PassedMoment(TimeUtil.parse(map.get(alias).toString(), timeUnit));
            }
        }
        throw new IllegalArgumentException("Could not parse time, missing key: " + Arrays.toString(aliases));
    }

    private void validTime(Map<?, ?> map, String... aliases) {
        for (String alias : aliases) {
            if (map.containsKey(alias)) {
                Preconditions.checkArgument(TimeUtil.validTime(map.get(alias).toString()), "Expected a number, or a time format for '" + alias + "' in cooking step!");
                return;
            }
        }
        throw new IllegalArgumentException("Expected a time with any of the following keys " + Arrays.toString(aliases));
    }

    private void checkStep(BrewingStep.StepType type, Map<?, ?> map, boolean hasParsedIngredientStep) throws IllegalArgumentException {
        switch (type) {
            case COOK -> {
                validTime(map, "time", "cook-time");
                Preconditions.checkArgument(hasParsedIngredientStep || map.get("ingredients") instanceof List, "Expected string list value for 'ingredients' in cook step!");
                Preconditions.checkArgument(!map.containsKey("cauldron-type") || map.get("cauldron-type") instanceof String, "Expected string value for 'cauldron-type' in cook step!");
                String cauldronType = (String) map.get("cauldron-type");
                Preconditions.checkArgument(cauldronType == null || BreweryRegistry.CAULDRON_TYPE.containsKey(BreweryKey.parse(cauldronType)), "Expected a valid cauldron type for 'cauldron-type' in cook step!");
            }
            case DISTILL ->
                    Preconditions.checkArgument(map.get("runs") instanceof Integer integer && integer > 0, "Expected a positive integer value for 'runs' in distill step!");
            case AGE -> {
                validTime(map, "time", "age-years", "aging-years");
                Preconditions.checkArgument(parseTime(map, TimeUtil.TimeUnit.AGING_YEARS, "time", "age-years", "aging-years").moment() > Config.config().barrels().agingYearTicks() / 2, "Expected a time longer than half an aging year for 'age-years' in age step!");
                Preconditions.checkArgument(!map.containsKey("barrel-type") || map.get("barrel-type") instanceof String, "Expected string value for 'barrel-type' in age step!");
                String barrelType = map.containsKey("barrel-type") ? (String) map.get("barrel-type") : "any";
                Preconditions.checkArgument(BreweryRegistry.BARREL_TYPE.containsKey(BreweryKey.parse(barrelType)), "Expected a valid barrel type for 'barrel-type' in age step!");
            }
            case MIX -> {
                validTime(map, "time", "mix-time");
                Preconditions.checkArgument(hasParsedIngredientStep || map.get("ingredients") instanceof List, "Expected string list value for 'ingredients' in mix step!");
                Preconditions.checkArgument(!map.containsKey("cauldron-type") || map.get("cauldron-type") instanceof String, "Expected string value for 'cauldron-type' in mix step!");
                String cauldronType = (String) map.get("cauldron-type");
                Preconditions.checkArgument(cauldronType == null || BreweryRegistry.CAULDRON_TYPE.containsKey(BreweryKey.parse(cauldronType)), "Expected a valid cauldron type for 'cauldron-type' in cook step!");
            }
        }
    }
}