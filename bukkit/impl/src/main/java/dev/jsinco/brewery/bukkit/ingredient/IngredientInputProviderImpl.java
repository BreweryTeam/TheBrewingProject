package dev.jsinco.brewery.bukkit.ingredient;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.ingredient.UncheckedIngredient;
import dev.jsinco.brewery.api.ingredient.IngredientInputProvider;
import dev.jsinco.brewery.api.ingredient.WildcardIngredient;
import dev.jsinco.brewery.api.util.BreweryKey;

public class IngredientInputProviderImpl implements IngredientInputProvider {
    @Override
    public UncheckedIngredient unchecked(BreweryKey key) {
        return new UncheckedIngredientImpl(key);
    }

    @Override
    public UncheckedIngredient unchecked(Ingredient ingredient) {
        return new UncheckedIngredientImpl(ingredient);
    }

    @Override
    public WildcardIngredient wildcard(String value) {
        return new WildcardIngredientImpl(value);
    }
}
