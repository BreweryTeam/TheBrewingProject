package dev.jsinco.brewery.bukkit.configuration.serializer;

import dev.jsinco.brewery.api.ingredient.IngredientInput;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.bukkit.ingredient.UncheckedIngredientImpl;
import dev.jsinco.brewery.bukkit.ingredient.WildcardIngredientImpl;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.kyori.adventure.key.Key;

public class IngredientInputSerializer implements ObjectSerializer<IngredientInput> {
    @Override
    public boolean supports(@NonNull Class<? super IngredientInput> type) {
        return IngredientInput.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@org.jspecify.annotations.NonNull IngredientInput object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(switch (object) {
            case UncheckedIngredientImpl uncheckedIngredient -> uncheckedIngredient.key().minimalized(Key.MINECRAFT_NAMESPACE);
            case WildcardIngredientImpl wildcardIngredient -> wildcardIngredient.value();
            default -> throw new IllegalArgumentException("Unknown serialization object type: " + object);
        });
    }

    @Override
    public IngredientInput deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String stringKey = data.getValue(String.class);
        if (stringKey == null) {
            return null;
        }
        // Wildcard
        if (stringKey.contains("*")) {
            return new WildcardIngredientImpl(stringKey);
        }
        BreweryKey key = BreweryKey.parse(stringKey, Key.MINECRAFT_NAMESPACE);
        return new UncheckedIngredientImpl(key);
    }
}
