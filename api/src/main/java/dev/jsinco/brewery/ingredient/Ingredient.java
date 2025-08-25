package dev.jsinco.brewery.ingredient;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an ingredient in a recipe.
 */
public interface Ingredient {

    /**
     * @return Key of the ingredient
     */
    @NotNull String getKey();

    /**
     * @return A component with a display name
     */
    @NotNull Component displayName();
}
