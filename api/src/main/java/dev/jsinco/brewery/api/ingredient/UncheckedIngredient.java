package dev.jsinco.brewery.api.ingredient;

import dev.jsinco.brewery.api.util.BreweryKey;
import net.kyori.adventure.key.Key;

import java.util.Optional;

public interface UncheckedIngredient extends IngredientInput {

    Optional<Ingredient> retrieve();

    BreweryKey key();

    /**
     * Only use when defining the ingredient, it will be converted when serializing and deserializing the config
     *
     * @param string key string representation
     */
    static UncheckedIngredient minecraft(String string) {
        return IngredientInputProviderHolder.instance()
                .unchecked(BreweryKey.parse(string, Key.MINECRAFT_NAMESPACE));
    }

}
