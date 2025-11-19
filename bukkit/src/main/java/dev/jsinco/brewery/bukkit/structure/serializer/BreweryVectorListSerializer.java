package dev.jsinco.brewery.bukkit.structure.serializer;

import dev.jsinco.brewery.api.vector.BreweryVector;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class BreweryVectorListSerializer implements ObjectSerializer<BreweryVector.List> {
    @Override
    public boolean supports(@NonNull Class<? super BreweryVector.List> type) {
        return BreweryVector.List.class == type;
    }

    @Override
    public void serialize(BreweryVector.@NonNull List object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValueCollection(object.elements(), BreweryVector.class);
    }

    @Override
    public BreweryVector.List deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        return new BreweryVector.List(data.getValueAsList(BreweryVector.class));
    }
}
