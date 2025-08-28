package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RecipeRegistryImpl<I> implements RecipeRegistry<I> {


    private Map<String, Recipe<I>> recipes = new ConcurrentHashMap<>();
    private Map<String, RecipeResult<I>> defaultRecipes = new HashMap<>();
    private List<RecipeResult<I>> defaultRecipeList = new ArrayList<>();
    private Set<Ingredient> allIngredients = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final Random RANDOM = new Random();

    public void registerRecipes(@NotNull Map<String, Recipe<I>> recipes) {
        this.recipes = new HashMap<>(recipes);
        recipes.values().stream()
                .map(this::getRecipeIngredients)
                .flatMap(Collection::stream)
                .forEach(allIngredients::add);

    }

    public Optional<Recipe<I>> getRecipe(@NotNull String recipeName) {
        Preconditions.checkNotNull(recipeName);
        return Optional.ofNullable(recipes.get(recipeName));
    }

    public Collection<Recipe<I>> getRecipes() {
        return recipes.values();
    }

    @Override
    public void registerRecipe(Recipe<I> recipe) {
        recipes.put(recipe.getRecipeName(), recipe);
        allIngredients.addAll(this.getRecipeIngredients(recipe));
    }

    @Override
    public void unRegisterRecipe(Recipe<I> recipe) {
        recipes.remove(recipe.getRecipeName());
        allIngredients = recipes.values().stream()
                .map(this::getRecipeIngredients)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public Optional<RecipeResult<I>> getDefaultRecipe(@NotNull String recipeName) {
        Preconditions.checkNotNull(recipeName);
        return Optional.ofNullable(defaultRecipes.get(recipeName));
    }

    private List<Ingredient> getRecipeIngredients(Recipe<?> recipe) {
        return recipe.getSteps()
                .stream()
                .filter(BrewingStep.IngredientsStep.class::isInstance)
                .map(BrewingStep.IngredientsStep.class::cast)
                .map(BrewingStep.IngredientsStep::ingredients)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public Collection<RecipeResult<I>> getDefaultRecipes() {
        return defaultRecipeList;
    }

    @Override
    public void registerDefaultRecipe(String name, RecipeResult<I> recipe) {
        defaultRecipes.put(name, recipe);
        defaultRecipeList.add(recipe);
    }

    @Override
    public void unRegisterDefaultRecipe(String name) {
        RecipeResult<I> defaultRecipe = defaultRecipes.remove(name);
        if (defaultRecipe == null) {
            return;
        }
        defaultRecipeList.remove(defaultRecipe);
    }

    public RecipeResult<I> getRandomDefaultRecipe() {
        return defaultRecipeList.get(RANDOM.nextInt(defaultRecipeList.size()));
    }

    public void registerDefaultRecipes(@NotNull Map<String, RecipeResult<I>> defaultRecipes) {
        this.defaultRecipes = Preconditions.checkNotNull(defaultRecipes);
        this.defaultRecipeList = List.copyOf(defaultRecipes.values());
    }

    @Override
    public boolean isRegisteredIngredient(Ingredient ingredient) {
        return allIngredients.stream().anyMatch(ingredient::equals);
    }

    @Override
    public Set<Ingredient> registeredIngredients() {
        return allIngredients;
    }
}
