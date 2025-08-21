package dev.jsinco.brewery.configuration;

import com.google.common.collect.ImmutableList;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.serdes.SerdesRegistry;
import lombok.NonNull;

import java.util.List;

public class OkaeriSerdesPackBuilder {

    ImmutableList.Builder<ObjectSerializer<?>> objectSerializers = new ImmutableList.Builder<>();

    public OkaeriSerdesPackBuilder add(ObjectSerializer objectSerializer) {
        objectSerializers.add(objectSerializer);
        return this;
    }

    public OkaeriSerdesPack build() {
        return new OkaeriSerdesPackImpl(objectSerializers.build());
    }

    private record OkaeriSerdesPackImpl(List<ObjectSerializer<?>> serializers) implements OkaeriSerdesPack {

        @Override
        public void register(@NonNull SerdesRegistry registry) {
            serializers.forEach(registry::register);
        }
    }
}
