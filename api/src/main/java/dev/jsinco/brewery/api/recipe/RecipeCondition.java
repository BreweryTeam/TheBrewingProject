package dev.jsinco.brewery.api.recipe;

import dev.jsinco.brewery.api.brew.BrewingStep;

import java.util.List;

public interface RecipeCondition {

    boolean matches(List<BrewingStep> expected, List<BrewingStep> actual);

    int complexity();
}
