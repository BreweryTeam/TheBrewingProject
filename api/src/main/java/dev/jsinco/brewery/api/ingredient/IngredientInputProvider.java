package dev.jsinco.brewery.api.ingredient;

import dev.jsinco.brewery.api.util.BreweryKey;

public interface IngredientInputProvider {

    UncheckedIngredient unchecked(BreweryKey key);

    UncheckedIngredient unchecked(Ingredient ingredient);

    WildcardIngredient wildcard(String value);
}
