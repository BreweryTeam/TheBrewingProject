package dev.jsinco.brewery.api.ingredient;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * Represents an ingredient in a recipe.
 */
@ApiStatus.NonExtendable
public interface Ingredient {

    /**
     * @return Key of the ingredient
     */
    @NotNull String getKey();

    /**
     * @return A component with a display name
     */
    @NotNull Component displayName();

    /**
     *
     * @param baseIngredientSet A set of base ingredients to match against
     * @return An optionally present ingredient, if found
     */
    Optional<? extends Ingredient> findMatch(Set<BaseIngredient> baseIngredientSet);

    /**
     * @return An ingredient without any extra data
     */
    BaseIngredient toBaseIngredient();
}
