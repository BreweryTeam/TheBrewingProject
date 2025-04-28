package dev.jsinco.brewery.recipes;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.recipe.Recipe;
import dev.jsinco.brewery.recipe.RecipeRegistry;
import dev.jsinco.brewery.recipe.RecipeResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RecipeRegistryImpl<I> implements RecipeRegistry<I> {


    private Map<String, Recipe<I>> recipes = new HashMap<>();
    private Map<String, RecipeResult<I>> defaultRecipes = new HashMap<>();
    private List<RecipeResult<I>> defaultRecipeList = new ArrayList<>();

    private static final Random RANDOM = new Random();

    public void registerRecipes(@NotNull Map<String, Recipe<I>> recipes) {
        this.recipes = new HashMap<>(recipes);
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
    }

    @Override
    public void unRegisterRecipe(Recipe<I> recipe) {
        recipes.remove(recipe.getRecipeName());
    }

    public Optional<RecipeResult<I>> getDefaultRecipe(@NotNull String recipeName) {
        Preconditions.checkNotNull(recipeName);
        return Optional.ofNullable(defaultRecipes.get(recipeName));
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
}
