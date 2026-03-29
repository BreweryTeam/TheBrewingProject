package dev.jsinco.brewery.configuration;

import com.google.common.collect.ImmutableList;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.OkaeriSerdes;
import eu.okaeri.configs.serdes.OkaeriSerdes;
import eu.okaeri.configs.serdes.SerdesRegistry;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class OkaeriSerdesBuilder {

    ImmutableList.Builder<ObjectSerializer<?>> objectSerializers = new ImmutableList.Builder<>();

    public OkaeriSerdesBuilder add(ObjectSerializer objectSerializer) {
        objectSerializers.add(objectSerializer);
        return this;
    }

    public OkaeriSerdes build() {
        return new OkaeriSerdesImpl(objectSerializers.build());
    }

    private record OkaeriSerdesImpl(List<ObjectSerializer<?>> serializers) implements OkaeriSerdes {

        @Override
        public void register(@NonNull SerdesRegistry registry) {
            serializers.forEach(registry::register);
        }
    }
}
