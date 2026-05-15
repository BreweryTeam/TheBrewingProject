package dev.jsinco.brewery.util;

import dev.jsinco.brewery.api.recipe.RecipeRegistry;

public interface RegistryProvider {

    RecipeRegistry<?> recipeRegistry();
}
