package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.BaseIngredient;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.IngredientGroup;
import dev.jsinco.brewery.api.recipe.DefaultRecipe;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeRegistryImpl<I> implements RecipeRegistry<I> {


    private Map<String, Recipe<I>> recipes = new ConcurrentHashMap<>();
    private Map<String, DefaultRecipe<I>> defaultRecipes = new HashMap<>();
    private List<DefaultRecipe<I>> defaultRecipeList = new ArrayList<>();
    private Set<BaseIngredient> allIngredients = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void registerRecipes(@NotNull Map<String, Recipe<I>> recipes) {
        this.recipes = new ConcurrentHashMap<>(recipes);
        recipes.values().stream()
                .map(this::getRecipeIngredients)
                .flatMap(Collection::stream)
                .forEach(allIngredients::add);

    }

    @Override
    public Optional<Recipe<I>> getRecipe(@NotNull String recipeName) {
        Preconditions.checkNotNull(recipeName);

        // Try case-sensitive first
        Recipe<I> recipe = recipes.get(recipeName);
        if (recipe != null) {
            return Optional.of(recipe);
        }

        // Then try case-insensitive
        for (Map.Entry<String, Recipe<I>> entry : recipes.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(recipeName)) {
                return Optional.of(entry.getValue());
            }
        }

        return Optional.empty();
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

    private List<BaseIngredient> getRecipeIngredients(Recipe<?> recipe) {
        return recipe.getSteps()
                .stream()
                .filter(BrewingStep.IngredientsStep.class::isInstance)
                .map(BrewingStep.IngredientsStep.class::cast)
                .map(BrewingStep.IngredientsStep::ingredients)
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .flatMap(ingredient -> {
                    if (ingredient instanceof IngredientGroup ingredientGroup) {
                        return ingredientGroup.alternatives().stream()
                                .map(Ingredient::toBaseIngredient);
                    }
                    return Stream.of(ingredient.toBaseIngredient());
                })
                .toList();
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
        return ingredient.findMatch(allIngredients)
                .isPresent();
    }

    @Override
    public Set<BaseIngredient> registeredIngredients() {
        return allIngredients;
    }

    public void clear() {
        recipes.clear();
        defaultRecipes.clear();
        defaultRecipeList.clear();
        allIngredients.clear();
    }
}
