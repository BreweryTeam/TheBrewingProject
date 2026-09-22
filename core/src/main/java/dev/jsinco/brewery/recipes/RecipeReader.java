package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientGroup;
import dev.jsinco.brewery.api.ingredient.IngredientProviderHolder;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NonNull;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeReader<I> {

    private static final Set<BrewingStep.StepType> INGREDIENT_STEPS = Set.of(BrewingStep.StepType.MIX, BrewingStep.StepType.COOK);

    private final File folder;
    private final RecipeResultReader<I> recipeResultReader;
    private final Map<String, Component> groupDisplayNames;

    public RecipeReader(File folder, RecipeResultReader<I> recipeResultReader) {
        this.folder = folder;
        this.recipeResultReader = recipeResultReader;
        this.groupDisplayNames = readGroupDisplayNames();
    }

    public List<IngredientGroup> findIngredientGroups() {
        try {
            List<IngredientGroup> output = new ArrayList<>();
            File recipeFolder = new File(folder, "recipes");
            if (recipeFolder.isDirectory()) {
                findIngredientGroups(recipeFolder)
                        .entrySet()
                        .stream()
                        .filter(entry -> !entry.getValue().isEmpty())
                        .map(entry -> new IngredientGroup(
                                new BreweryKey("#brewery", entry.getKey()),
                                groupDisplayNames.getOrDefault(entry.getKey(), Component.text(entry.getKey())),
                                entry.getValue().stream()
                                        .map(BreweryKey::parse)
                                        .map(IngredientProviderHolder.instance()::breweryIngredient)
                                        .map(Ingredient.class::cast)
                                        .toList()
                        )).forEach(output::add);
            }
            return output;
        } catch (Exception e) {
            Logger.logErr(e);
            return List.of();
        }
    }

    private Map<String, Set<String>> findIngredientGroups(File folder) {
        Map<String, Set<String>> ingredientGroups = new HashMap<>();
        File[] recipesInRecipeFolder = folder.listFiles();
        if (recipesInRecipeFolder == null) {
            return ingredientGroups;
        }
        for (File recipeFile : recipesInRecipeFolder) {
            if (recipeFile.isDirectory()) {
                Map<String, Set<String>> childGroup = findIngredientGroups(recipeFile);
                ingredientGroups.put(recipeFile.getName().toLowerCase(Locale.ROOT), childGroup.values()
                        .stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet())
                );
                ingredientGroups.putAll(childGroup);
                continue;
            }
            ingredientGroups.put(recipeFile.getName().toLowerCase(Locale.ROOT).replaceAll("\\.yml", ""), listRecipes(recipeFile));
        }
        return ingredientGroups;
    }

    private Set<String> listRecipes(File file) {
        if (!file.isFile()) {
            return Set.of();
        }
        YamlFile recipesFile = new YamlFile(file);

        try {
            recipesFile.createOrLoadWithComments();
        } catch (Exception e) {
            Logger.logErr(e);
            return Set.of();
        }

        if (!recipesFile.getBoolean("enabled", true)) {
            return Set.of();
        }

        ConfigurationSection recipesSection = recipesFile.getConfigurationSection("recipes");
        if (recipesSection == null) {
            return Set.of();
        }
        return recipesSection.getKeys(false)
                .stream()
                .filter(recipesSection::isConfigurationSection)
                .filter(key -> validRecipeFormat(recipesSection.getConfigurationSection(key)))
                .collect(Collectors.toSet());
    }

    private boolean validRecipeFormat(ConfigurationSection recipeSection) {
        try {
            boolean hasParsedIngredientStep = false;
            for (Map<?, ?> step : recipeSection.getMapList("steps")) {
                BrewingStep.StepType type = BrewingStep.StepType.valueOf(
                        String.valueOf(step.get("type")).toUpperCase(Locale.ROOT)
                );
                checkStep(type, step, hasParsedIngredientStep);
                hasParsedIngredientStep |= INGREDIENT_STEPS.contains(type);
            }
            recipeResultReader.readRecipeResults(recipeSection);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public CompletableFuture<List<RecipeGroup<I>>> readRecipeGroups(CompletableFuture<ResolvedIngredientManager<I>> ingredientManager) {
        return ingredientManager.thenApply(
                resolvedIngredientManager -> {
                    try {
                        List<RecipeFileContext<I>> groups = new ArrayList<>(readRecipeGroups(new File(folder, "recipes"), resolvedIngredientManager, List.of()));

                        File recipesFile = new File(folder, "recipes.yml");
                        if (recipesFile.isFile()) {
                            readRecipeFile(resolvedIngredientManager, recipesFile, List.of("recipes"))
                                    .ifPresent(groups::add);
                        }
                        return compileGroups(groups);
                    } catch (Throwable e) {
                        Logger.logErr(e);
                        throw e;
                    }
                }
        );
    }

    private List<RecipeFileContext<I>> readRecipeGroups(File recipeFolder, ResolvedIngredientManager<I> resolvedIngredientManager, List<String> parentId) {
        List<RecipeFileContext<I>> groups = new ArrayList<>();
        File[] recipesInRecipeFolder = recipeFolder.listFiles();
        if (recipesInRecipeFolder == null) {
            return groups;
        }
        for (File recipeFile : recipesInRecipeFolder) {
            String id = recipeFile.getName()
                    .replaceAll("\\.yml$", "")
                    .toLowerCase(Locale.ROOT);
            List<String> groupIds = branch(parentId, id);
            if (recipeFile.isDirectory()) {
                groups.addAll(readRecipeGroups(
                        recipeFile,
                        resolvedIngredientManager,
                        groupIds
                ));
                continue;
            }
            if (!recipeFile.isFile() || !recipeFile.getName().endsWith(".yml")) {
                continue;
            }
            readRecipeFile(
                    resolvedIngredientManager,
                    recipeFile,
                    groupIds
            ).ifPresent(groups::add);
        }
        return groups;
    }

    private List<RecipeGroup<I>> compileGroups(List<RecipeFileContext<I>> recipeFiles) {
        Map<String, List<Recipe<I>>> groupsWithSameId = new HashMap<>();
        Map<String, List<String>> takenRecipeIds = new HashMap<>();
        for (RecipeFileContext<I> recipeFile : recipeFiles) {
            for (Recipe<I> recipe : recipeFile.recipes()) {
                takenRecipeIds.computeIfAbsent(recipe.getRecipeName(), ignored -> new ArrayList<>())
                        .add(recipeFile.path().getPath());
            }
            for (String id : recipeFile.groupIds()) {
                groupsWithSameId.computeIfAbsent(id, ignored -> new ArrayList<>())
                        .addAll(recipeFile.recipes());
            }
        }
        Set<String> clashingRecipes = takenRecipeIds
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        for (String clashingRecipe : clashingRecipes) {
            Logger.logWarn("Unregistering clashing recipes with key '%s': Found in the following files"
                    .formatted(clashingRecipe)
            );
            takenRecipeIds.get(clashingRecipe)
                    .stream()
                    .map("- %s"::formatted)
                    .forEach(Logger::logWarn);
        }

        List<RecipeGroup<I>> recipeGroups = new ArrayList<>();
        for (Map.Entry<String, List<Recipe<I>>> entry : groupsWithSameId.entrySet()) {
            List<Recipe<I>> recipes = entry.getValue()
                    .stream()
                    .filter(recipe -> !clashingRecipes.contains(recipe.getRecipeName()))
                    .toList();
            if (recipes.isEmpty()) {
                Logger.logWarn("Group '%s' has no recipes - Unregistering");
                continue;
            }
            recipeGroups.add(new RecipeGroupImpl<>(
                    entry.getKey(),
                    groupDisplayNames.getOrDefault(entry.getKey(), Component.text(entry.getKey())),
                    recipes
            ));
        }
        return recipeGroups;
    }

    private Map<String, Component> readGroupDisplayNames() {
        Map<String, Component> groupDisplayNames = new HashMap<>();
        try {
            File groupsFile = new File(folder, "recipes/groups.yml");
            if (!groupsFile.isFile()) {
                return groupDisplayNames;
            }
            YamlFile groupsYamlFile = new YamlFile(groupsFile);
            groupsYamlFile.load();
            ConfigurationSection displayNames = groupsYamlFile.getConfigurationSection("display-name");
            for (Map.Entry<String, Object> entry : displayNames.getMapValues(false).entrySet()) {
                if (!(entry.getValue() instanceof String displayNameString)) {
                    Logger.logWarn(
                            "Invalid entry in '%s': Expected a string value for display-name of group '%s'"
                                    .formatted(groupsFile.getPath(), entry.getKey())
                    );
                    continue;
                }
                String sanitizedGroupId = entry.getKey().toLowerCase(Locale.ROOT);
                if (groupDisplayNames.containsKey(sanitizedGroupId)) {
                    Logger.logWarn("Invalid entry in '%s': Duplicate keys (case insensitive)"
                            .formatted(groupsFile.getPath())
                    );
                    continue;
                }
                groupDisplayNames.put(sanitizedGroupId, MiniMessage.miniMessage().deserialize(displayNameString));
            }
        } catch (Exception ignored) {
        }
        return groupDisplayNames;
    }

    private List<String> branch(List<String> parent, String child) {
        return Stream.concat(
                parent.stream(),
                Stream.of(child)
        ).toList();
    }

    private Optional<RecipeFileContext<I>> readRecipeFile(ResolvedIngredientManager<I> resolvedIngredientManager, File path, List<String> ids) {
        YamlFile recipesFile = new YamlFile(path);

        try {
            recipesFile.createOrLoadWithComments();
        } catch (Exception e) {
            Logger.logErr(e);
            return Optional.empty();
        }

        if (!recipesFile.getBoolean("enabled", true)) {
            return Optional.empty();
        }

        ConfigurationSection recipesSection = recipesFile.getConfigurationSection("recipes");
        if (recipesSection == null) {
            return Optional.empty();
        }
        List<Recipe<I>> recipes = recipesSection.getKeys(false)
                .stream()
                .map(key -> getRecipe(recipesSection.getConfigurationSection(key), key, resolvedIngredientManager, path.getPath()))
                .flatMap(Optional::stream)
                .map(recipeImpl -> (Recipe<I>) recipeImpl)
                .toList();
        return Optional.of(new RecipeFileContext<>(
                recipes,
                ids,
                path
        ));
    }

    /**
     * Obtain a recipe from the recipes.yml file.
     *
     * @param recipeName The name/id of the recipe to obtain. Ex: 'example_recipe'
     * @return A Recipe object with all the attributes of the recipe.
     */
    private Optional<RecipeImpl<I>> getRecipe(ConfigurationSection recipe, String recipeName, ResolvedIngredientManager<I> resolvedIngredientManager, String fileContext) {
        try {
            List<BrewingStep> steps = parseSteps(recipe.getMapList("steps"), resolvedIngredientManager);
            return Optional.of(new RecipeImpl.Builder<I>(recipeName)
                    .brewDifficulty(recipe.getDouble("brew-difficulty", 1D))
                    .recipeResults(recipeResultReader.readRecipeResults(recipe))
                    .steps(steps)
                    .build()
            );
        } catch (Throwable throwable) {
            Logger.logErr("Could not read recipe '%s' in file '%s'".formatted(recipeName, fileContext));
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
            hasParsedIngredientStep |= INGREDIENT_STEPS.contains(type);
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