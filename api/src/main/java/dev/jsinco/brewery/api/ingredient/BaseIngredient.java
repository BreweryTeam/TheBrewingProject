package dev.jsinco.brewery.api.ingredient;

import java.util.Optional;
import java.util.Set;

/**
 * Easily hashable ingredient, separate class for clarity
 */
public interface BaseIngredient extends Ingredient {

    default BaseIngredient toBaseIngredient() {
        return this;
    }

    default Optional<? extends Ingredient> findMatch(Set<BaseIngredient> baseIngredientSet) {
        return Optional.of(this)
                .filter(baseIngredientSet::contains);
    }
}
