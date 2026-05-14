package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.ingredient.BaseIngredient;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.recipe.DefaultRecipe;
import dev.jsinco.brewery.api.recipe.Recipe;
import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.util.BrewUtil;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeRegistryImpl<I> implements RecipeRegistry<I> {


    private final Map<String, Recipe<I>> recipes = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<String, DefaultRecipe<I>> defaultRecipes = Collections.synchronizedMap(new LinkedHashMap<>());
    private final Map<BaseIngredient, Set<Recipe<I>>> baseIngredientToRecipes = new ConcurrentHashMap<>();


    public void registerRecipes(@NonNull Map<String, Recipe<I>> recipes) {
        this.clear();
        recipes.forEach((_, recipe) -> registerRecipe(recipe));
    }

    @Override
    public Optional<Recipe<I>> getRecipe(@NonNull String recipeName) {
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
    public Collection<Recipe<I>> possibleRecipes(List<BrewingStep> steps) {
        Set<Recipe<I>> output = new HashSet<>();
        for (List<BrewingStep> stepVariation : BrewUtil.variations(steps, this)) {
            Set<Recipe<I>> recipes = null;
            for (BaseIngredient baseIngredient : BrewUtil.getRecipeIngredients(stepVariation)) {
                if (recipes == null) {
                    recipes = baseIngredientToRecipes.getOrDefault(baseIngredient, Set.of());
                }
                if (recipes.isEmpty()) {
                    return recipes;
                }
                recipes.removeIf(recipe -> !BrewUtil.getRecipeIngredients(recipe)
                        .contains(baseIngredient)
                );
            }
            if (recipes != null) {
                output.addAll(recipes);
            }
        }
        return output;
    }

    @Override
    public void registerRecipe(Recipe<I> recipe) {
        recipes.put(recipe.getRecipeName(), recipe);
        for (BaseIngredient recipeIngredient : BrewUtil.getRecipeIngredients(recipe)) {
            baseIngredientToRecipes.computeIfAbsent(recipeIngredient, ignored -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                    .add(recipe);
        }
    }

    @Override
    public void unRegisterRecipe(Recipe<I> recipe) {
        recipes.remove(recipe.getRecipeName());
        for (BaseIngredient baseIngredient : BrewUtil.getRecipeIngredients(recipe)) {
            Set<Recipe<I>> ingredientRecipes = baseIngredientToRecipes.get(baseIngredient);
            if (ingredientRecipes == null) {
                continue;
            }
            ingredientRecipes.removeIf(recipe0 -> recipe0.getRecipeName().equals(recipe.getRecipeName()));
            if (ingredientRecipes.isEmpty()) {
                baseIngredientToRecipes.remove(baseIngredient);
            }
        }
    }

    @Override
    public Optional<DefaultRecipe<I>> getDefaultRecipe(@NonNull String recipeName) {
        Preconditions.checkNotNull(recipeName);
        return Optional.ofNullable(defaultRecipes.get(recipeName));
    }

    @Override
    public Collection<DefaultRecipe<I>> getDefaultRecipes() {
        return defaultRecipes.values();
    }

    @Override
    public void registerDefaultRecipe(String name, DefaultRecipe<I> recipe) {
        if (recipe == null) {
            Logger.logWarn("Default recipe was null, ignoring: " + name);
            return;
        }
        defaultRecipes.put(name, recipe);
    }

    @Override
    public void unRegisterDefaultRecipe(String name) {
        defaultRecipes.remove(name);
    }

    @Override
    public boolean isRegisteredIngredient(Ingredient ingredient) {
        return ingredient.findMatch(baseIngredientToRecipes.keySet())
                .isPresent();
    }

    @Override
    public Set<BaseIngredient> registeredIngredients() {
        return baseIngredientToRecipes.keySet();
    }

    public void clear() {
        recipes.clear();
        defaultRecipes.clear();
        baseIngredientToRecipes.clear();
    }
}
