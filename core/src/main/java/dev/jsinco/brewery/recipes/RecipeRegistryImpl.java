package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.recipe.DefaultRecipe;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RecipeRegistryImpl<I> implements RecipeRegistry<I> {


    private Map<String, Recipe<I>> recipes = new ConcurrentHashMap<>();
    private Map<String, DefaultRecipe<I>> defaultRecipes = new HashMap<>();
    private List<DefaultRecipe<I>> defaultRecipeList = new ArrayList<>();
    private Set<Ingredient> allIngredients = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final Random RANDOM = new Random();

    public void registerRecipes(@NotNull Map<String, Recipe<I>> recipes) {
        this.recipes = new HashMap<>(recipes);
        recipes.values().stream()
                .map(this::getRecipeIngredients)
                .flatMap(Collection::stream)
                .forEach(allIngredients::add);

    }

    @Override
    public Optional<Recipe<I>> getRecipe(@NotNull String recipeName) {
        Preconditions.checkNotNull(recipeName);
        return Optional.ofNullable(recipes.get(recipeName));
    }

    @Override
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

    @Override
    public Optional<DefaultRecipe<I>> getDefaultRecipe(@NotNull String recipeName) {
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

    @Override
    public Collection<DefaultRecipe<I>> getDefaultRecipes() {
        return defaultRecipeList;
    }

    @Override
    public void registerDefaultRecipe(String name, DefaultRecipe<I> recipe) {
        defaultRecipes.put(name, recipe);
        defaultRecipeList.add(recipe);
    }

    @Override
    public void unRegisterDefaultRecipe(String name) {
        DefaultRecipe<I> defaultRecipe = defaultRecipes.remove(name);
        if (defaultRecipe == null) {
            return;
        }
        defaultRecipeList.remove(defaultRecipe);
    }

    @Override
    public boolean isRegisteredIngredient(Ingredient ingredient) {
        return allIngredients.stream().anyMatch(ingredient::equals);
    }

    @Override
    public Set<Ingredient> registeredIngredients() {
        return allIngredients;
    }

    public void clear() {
        recipes.clear();
        defaultRecipes.clear();
        defaultRecipeList.clear();
        allIngredients.clear();
    }
}
