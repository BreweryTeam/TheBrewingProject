package dev.jsinco.brewery.api.ingredient;

import dev.jsinco.brewery.api.util.BreweryKey;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface IngredientProvider {

    UncheckedIngredient unchecked(BreweryKey key);

    UncheckedIngredient unchecked(Ingredient ingredient);

    WildcardIngredient wildcard(String value);

    BaseIngredient breweryIngredient(BreweryKey key);
}
