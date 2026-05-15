package dev.jsinco.brewery.bukkit.util;

import dev.jsinco.brewery.api.recipe.RecipeRegistry;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.util.RegistryProvider;

public class RegistryProviderImpl implements RegistryProvider {
    @Override
    public RecipeRegistry<?> recipeRegistry() {
        return TheBrewingProject.getInstance().getRecipeRegistry();
    }
}
