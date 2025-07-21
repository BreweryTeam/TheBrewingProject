package dev.jsinco.brewery.configuration.serializers;

import dev.jsinco.brewery.event.CustomEvent;
import dev.jsinco.brewery.event.CustomEventRegistry;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Logger;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.Collection;

public class EventRegistrySerializer implements ObjectSerializer<CustomEventRegistry> {

    @Override
    public boolean supports(@NonNull Class<? super CustomEventRegistry> type) {
        return CustomEventRegistry.class == type;
    }

    @Override
    public void serialize(@NonNull CustomEventRegistry object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {

    }

    @Override
    public CustomEventRegistry deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        CustomEventRegistry output = new CustomEventRegistry();
        Collection<String> children = data.asMap().keySet();
        for (String child : children) {
            try {
                output.registerCustomEvent(new CustomEvent.Keyed(data.get(child, CustomEvent.class), BreweryKey.parse(child)));
            } catch (IllegalArgumentException e) {
                Logger.logErr("Exception when reading custom event: " + child);
                throw new IllegalArgumentException(e);
            }
        }
        return output;
    }
}
