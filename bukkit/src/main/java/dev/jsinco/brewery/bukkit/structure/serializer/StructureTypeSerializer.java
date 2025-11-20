package dev.jsinco.brewery.bukkit.structure.serializer;

import dev.jsinco.brewery.api.structure.StructureType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class StructureTypeSerializer implements ObjectSerializer<StructureType> {
    @Override
    public boolean supports(@NonNull Class<? super StructureType> type) {
        return StructureType.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull StructureType object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(object.key().minimalized());
    }

    @Override
    public StructureType deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        BreweryKey key = BreweryKey.parse(data.getValue(String.class));
        return BreweryRegistry.STRUCTURE_TYPE.get(key);
    }
}
