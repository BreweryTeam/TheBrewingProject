package dev.jsinco.brewery.configuration.serializers;

import dev.jsinco.brewery.api.math.RangeD;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class RangeDSerializer implements ObjectSerializer<RangeD> {
    @Override
    public boolean supports(@NonNull Class<? super RangeD> type) {
        return type == RangeD.class;
    }

    @Override
    public void serialize(@NonNull RangeD object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        if (object.min() != null) {
            data.add("min", object.min());
        }
        if (object.max() != null) {
            data.add("max", object.max());
        }
    }

    @Override
    public RangeD deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        Double min = data.get("min", Double.class);
        Double max = data.get("max", Double.class);
        return new RangeD(min, max);
    }
}
