package dev.jsinco.brewery.api.recipe;

import java.util.List;

public record DefaultRecipe<I>(RecipeResult<I> result, List<RecipeCondition> recipeConditions, boolean onlyRuinedBrews) {

    public int complexity() {
        return recipeConditions.stream()
                .map(RecipeCondition::complexity)
                .reduce(0, Integer::sum);
    }
}
