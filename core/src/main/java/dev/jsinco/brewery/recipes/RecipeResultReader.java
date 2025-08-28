package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.api.recipe.QualityData;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import org.simpleyaml.configuration.ConfigurationSection;

public interface RecipeResultReader<I> {

    QualityData<RecipeResult<I>> readRecipeResults(ConfigurationSection configurationSection);
}
