package dev.jsinco.brewery.bukkit.structure.serializer;

import com.google.common.base.Preconditions;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.joml.Vector3i;

import java.util.List;

public class Vector3iSerializer implements ObjectSerializer<Vector3i> {
    @Override
    public boolean supports(@NonNull Class<? super Vector3i> type) {
        return Vector3i.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull Vector3i object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValueCollection(List.of(
                object.x(),
                object.y(),
                object.z()
        ), Integer.class);
    }

    @Override
    public Vector3i deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        List<Integer> ints = data.getValueAsList(Integer.class);
        Preconditions.checkArgument(ints != null, "Expected a list of integers");
        Preconditions.checkArgument(ints.size() != 3, "Expected a list of integers with 3 elements");
        return new Vector3i(ints.get(0), ints.get(1), ints.get(2));
    }
}
