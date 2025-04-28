package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.recipe.RecipeResult;
import org.simpleyaml.configuration.ConfigurationSection;

public interface RecipeResultReader<I> {

    RecipeResult<I> readRecipeResult(ConfigurationSection configurationSection);
}
