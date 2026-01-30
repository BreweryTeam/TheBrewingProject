package dev.jsinco.brewery.bukkit.configuration.serializer;

import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.bukkit.configuration.UncheckedIngredientImpl;
import dev.jsinco.brewery.configuration.UncheckedIngredient;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.kyori.adventure.key.Key;

public class UncheckedIngredientSerializer implements ObjectSerializer<UncheckedIngredient> {

    @Override
    public boolean supports(@NonNull Class<? super UncheckedIngredient> type) {
        return UncheckedIngredient.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@org.jspecify.annotations.NonNull UncheckedIngredient object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(object.key().minimalized(Key.MINECRAFT_NAMESPACE));
    }

    @Override
    public UncheckedIngredient deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String stringKey = data.getValue(String.class);
        if (stringKey == null) {
            return null;
        }
        BreweryKey key = BreweryKey.parse(stringKey, Key.MINECRAFT_NAMESPACE);
        // Wildcard
        if(key.key().equals("*")) {
            return new UncheckedIngredient.NeverCompleting(key);
        }
        return new UncheckedIngredientImpl(key);
    }
}
