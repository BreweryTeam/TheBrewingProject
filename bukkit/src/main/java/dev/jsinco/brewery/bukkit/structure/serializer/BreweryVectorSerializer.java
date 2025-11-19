package dev.jsinco.brewery.bukkit.structure.serializer;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.vector.BreweryVector;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.List;

public class BreweryVectorSerializer implements ObjectSerializer<BreweryVector> {
    @Override
    public boolean supports(@NonNull Class<? super BreweryVector> type) {
        return BreweryVector.class == type;
    }

    @Override
    public void serialize(@NonNull BreweryVector object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValueCollection(List.of(object.x(), object.y(), object.z()), Integer.class);
    }

    @Override
    public BreweryVector deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        List<Integer> ints = data.getValueAsList(Integer.class);
        Preconditions.checkArgument(ints != null, "Expected a list of integers");
        Preconditions.checkArgument(ints.size() != 3, "Expected a list of integers with 3 elements");
        return new BreweryVector(ints.get(0), ints.get(1), ints.get(2));
    }
}
