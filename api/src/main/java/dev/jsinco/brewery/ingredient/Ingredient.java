package dev.jsinco.brewery.ingredient;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an ingredient in a recipe.
 */
public interface Ingredient {

    @NotNull String getKey();

    @NotNull Component displayName();
}
