package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.api.recipe.Recipe;

import java.io.File;
import java.util.List;

public record RecipeFileContext<I>(List<Recipe<I>> recipes, List<String> groupIds, File path) {
}
