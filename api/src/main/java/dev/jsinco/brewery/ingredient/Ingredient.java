package dev.jsinco.brewery.ingredient;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an ingredient in a recipe.
 */
public interface Ingredient {

    @NotNull String getKey();

    @NotNull String displayName();
}
