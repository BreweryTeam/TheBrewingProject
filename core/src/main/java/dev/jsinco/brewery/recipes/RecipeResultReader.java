package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.recipe.QualityData;
import dev.jsinco.brewery.recipe.RecipeResult;
import org.simpleyaml.configuration.ConfigurationSection;

public interface RecipeResultReader<I> {

    QualityData<RecipeResult<I>> readRecipeResults(ConfigurationSection configurationSection);
}
