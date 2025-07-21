package dev.jsinco.brewery.configuration.serializers;

import dev.jsinco.brewery.moment.Interval;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class IntervalSerializer implements ObjectSerializer<Interval> {

    @Override
    public boolean supports(@NonNull Class<? super Interval> type) {
        return Interval.class == type;
    }

    @Override
    public void serialize(@NonNull Interval object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(object.asString());
    }

    @Override
    public Interval deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String aString = data.getValue(String.class);
        return aString == null ? null : Interval.parseString(aString);
    }
}
