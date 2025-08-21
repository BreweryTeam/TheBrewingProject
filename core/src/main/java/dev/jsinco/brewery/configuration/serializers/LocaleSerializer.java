package dev.jsinco.brewery.configuration.serializers;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.Locale;

public class LocaleSerializer implements ObjectSerializer<Locale> {


    @Override
    public boolean supports(@NonNull Class<? super Locale> type) {
        return Locale.class == type;
    }

    @Override
    public void serialize(@NonNull Locale object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(object.toLanguageTag());
    }

    @Override
    public Locale deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        return Locale.forLanguageTag(data.getValue(String.class));
    }
}
