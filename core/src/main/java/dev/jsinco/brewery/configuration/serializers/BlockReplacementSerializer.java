package dev.jsinco.brewery.configuration.serializers;

import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Materials;
import dev.jsinco.brewery.configuration.structure.BlockReplacement;
import dev.jsinco.brewery.configuration.structure.GenericBlockReplacement;
import dev.jsinco.brewery.configuration.structure.KeyedBlockReplacement;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BlockReplacementSerializer implements ObjectSerializer<BlockReplacement> {
    @Override
    public boolean supports(@NonNull Class<?> type) {
        return BlockReplacement.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull BlockReplacement object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        object.write(data);
    }

    @Override
    public BlockReplacement deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        Set<String> keys = data.asMap().keySet();
        try {
            Map<BreweryKey, GenericBlockReplacement> readData = new HashMap<>();
            for (String key : keys) {
                Map<String, Materials> declaration = data.getAsMap(key, String.class, Materials.class);
                readData.put(BreweryKey.parse(key), new GenericBlockReplacement(declaration));
            }
            return new KeyedBlockReplacement(readData);
        } catch (IllegalArgumentException e) {
            Map<String, Materials> readData = new HashMap<>();
            keys.forEach(key ->
                    readData.put(key, data.get(key, Materials.class))
            );
            return new GenericBlockReplacement(readData);
        }
    }
}
