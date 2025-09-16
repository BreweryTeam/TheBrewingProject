package dev.jsinco.brewery.api.recipe;

public record DefaultRecipe<I>(RecipeResult<I> result, RecipeCondition recipeCondition, boolean onlyRuinedBrews) {
}
