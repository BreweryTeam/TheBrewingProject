package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.api.recipe.QualityData;
import dev.jsinco.brewery.api.recipe.RecipeResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;

@NullMarked
public interface RecipeResultReader<I> {

    QualityData<RecipeResult<I>> readRecipeResults(ConfigurationSection configurationSection, @Nullable String recipeName);
}
