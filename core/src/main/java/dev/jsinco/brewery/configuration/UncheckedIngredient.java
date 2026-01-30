package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.util.BreweryKey;
import net.kyori.adventure.key.Key;

import java.util.Optional;

public interface UncheckedIngredient {

    Optional<Ingredient> retrieve();

    BreweryKey key();

    /**
     * Only use when defining the ingredient, it will be converted when serializing and deserializing the config
     * @param string key string representation
     */
    static UncheckedIngredient minecraft(String string) {
        return new UncheckedIngredient.NeverCompleting(
                BreweryKey.parse(string, Key.MINECRAFT_NAMESPACE)
        );
    }

    /**
     * Only use when defining the ingredient, it will be converted when serializing and deserializing the config
     * @param key brewery key
     */
    record NeverCompleting(BreweryKey key) implements UncheckedIngredient {

        @Override
        public Optional<Ingredient> retrieve() {
            return Optional.empty();
        }
    }
}
