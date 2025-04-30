package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.brew.BrewingStep;
import dev.jsinco.brewery.ingredient.IngredientManager;
import dev.jsinco.brewery.recipe.Recipe;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Logging;
import dev.jsinco.brewery.util.Registry;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.moment.PassedMoment;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeReader<I> {

    private final File folder;
    private final RecipeResultReader<I> recipeResultReader;
    private final IngredientManager<I> ingredientManager;

    public RecipeReader(File folder, RecipeResultReader<I> recipeResultReader, IngredientManager<I> ingredientManager) {
        this.folder = folder;
        this.recipeResultReader = recipeResultReader;
        this.ingredientManager = ingredientManager;
    }

    public Map<String, Recipe<I>> readRecipes() {
        Path mainDir = folder.toPath();
        YamlFile recipesFile = new YamlFile(mainDir.resolve("recipes.yml").toFile());

        try {
            recipesFile.createOrLoadWithComments();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ConfigurationSection recipesSection = recipesFile.getConfigurationSection("recipes");
        ImmutableMap.Builder<String, Recipe<I>> recipes = new ImmutableMap.Builder<>();
        for (String recipeName : recipesSection.getKeys(false)) {
            try {
                recipes.put(recipeName, getRecipe(recipesSection.getConfigurationSection(recipeName), recipeName));
            } catch (Exception e) {
                Logging.error("Exception when reading recipe: " + recipeName);
                e.printStackTrace();
            }
        }
        return recipes.build();
    }

    /**
     * Obtain a recipe from the recipes.yml file.
     *
     * @param recipeName The name/id of the recipe to obtain. Ex: 'example_recipe'
     * @return A Recipe object with all the attributes of the recipe.
     */
    private RecipeImpl<I> getRecipe(ConfigurationSection recipe, String recipeName) {
        return new RecipeImpl.Builder<I>(recipeName)
                .brewDifficulty(recipe.getDouble("brew-difficulty", 1D))
                .recipeResult(recipeResultReader.readRecipeResult(recipe))
                .steps(parseSteps(recipe.getMapList("steps")))
                .build();
    }

    private @NotNull List<BrewingStep> parseSteps(List<Map<?, ?>> steps) {
        return steps.stream()
                .map(this::parseStep)
                .toList();
    }

    private BrewingStep parseStep(Map<?, ?> map) {
        BrewingStep.StepType type = BrewingStep.StepType.valueOf(String.valueOf(map.get("type")).toUpperCase(Locale.ROOT));
        checkStep(type, map);
        return switch (type) {
            case COOK -> new BrewingStep.Cook(
                    new PassedMoment(((Integer) map.get("cook-time")).longValue() * PassedMoment.MINUTE),
                    ingredientManager.getIngredientsWithAmount((List<String>) map.get("ingredients")),
                    Registry.CAULDRON_TYPE.get(BreweryKey.parse(map.containsKey("cauldron-type") ? map.get("cauldron-type").toString().toLowerCase(Locale.ROOT) : "water"))
            );
            case DISTILL -> new BrewingStep.Distill(
                    (int) map.get("runs")
            );
            case AGE -> new BrewingStep.Age(
                    new PassedMoment(((Integer) map.get("age-years")).longValue() * Moment.AGING_YEAR),
                    Registry.BARREL_TYPE.get(BreweryKey.parse(map.get("barrel-type").toString().toLowerCase(Locale.ROOT)))
            );
            case MIX -> new BrewingStep.Mix(
                    new PassedMoment(((Integer) map.get("mix-time")).longValue() * Moment.MINUTE),
                    ingredientManager.getIngredientsWithAmount((List<String>) map.get("ingredients"))
            );
        };
    }

    private void checkStep(BrewingStep.StepType type, Map<?, ?> map) throws IllegalArgumentException {
        switch (type) {
            case COOK -> {
                Preconditions.checkArgument(map.get("cook-time") instanceof Integer, "Expected integer value for 'cook-time' in cook step!");
                Preconditions.checkArgument(map.get("ingredients") instanceof List, "Expected string list value for 'ingredients' in cook step!");
                Preconditions.checkArgument(!map.containsKey("cauldron-type") || map.get("cauldron-type") instanceof String, "Expected string value for 'cauldron-type' in cook step!");
                String cauldronType = map.containsKey("cauldron-type") ? (String) map.get("cauldron-type") : "water";
                Preconditions.checkArgument(Registry.CAULDRON_TYPE.containsKey(BreweryKey.parse(cauldronType)), "Expected a valid cauldron type for 'cauldron-type' in cook step!");
            }
            case DISTILL ->
                    Preconditions.checkArgument(map.get("runs") instanceof Integer, "Expected integer value for 'runs' in distill step!");
            case AGE -> {
                Preconditions.checkArgument(map.get("age-years") instanceof Integer, "Expected integer value for 'age-years' in age step!");
                Preconditions.checkArgument(!map.containsKey("barrel-type") || map.get("barrel-type") instanceof String, "Expected string value for 'barrel-type' in age step!");
                String barrelType = map.containsKey("barrel-type") ? (String) map.get("barrel-type") : "any";
                Preconditions.checkArgument(Registry.BARREL_TYPE.containsKey(BreweryKey.parse(barrelType)), "Expected a valid barrel type for 'barrel-type' in age step!");
            }
            case MIX -> {
                Preconditions.checkArgument(map.get("mix-time") instanceof Integer, "Expected integer value for 'mix-time' in mix step!");
                Preconditions.checkArgument(map.get("ingredients") instanceof List, "Expected string list value for 'ingredients' in mix step!");
            }
        }
    }

    public static int parseAlcoholString(String str) {
        return Integer.parseInt(str.replace("%", "").replace(" ", ""));
    }
}