package dev.jsinco.brewery.configuration.serializers;

import dev.jsinco.brewery.api.event.CustomEvent;
import dev.jsinco.brewery.api.event.CustomEventRegistry;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.Logger;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public class EventRegistrySerializer implements ObjectSerializer<CustomEventRegistry> {

    @Override
    public boolean supports(@NonNull Class<? super CustomEventRegistry> type) {
        return CustomEventRegistry.class == type;
    }

    @Override
    public void serialize(@NonNull CustomEventRegistry object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        for (CustomEvent.Keyed event : object.events()) {
            String key = event.key().namespace().equalsIgnoreCase("brewery") ? event.key().key() : event.key().toString();
            data.add(key, event.event());
        }
    }

    @Override
    public CustomEventRegistry deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        CustomEventRegistry output = new CustomEventRegistry();
        Collection<String> children = data.asMap().keySet();
        for (String child : children) {
            try {
                output.registerCustomEvent(new CustomEvent.Keyed(data.get(child, CustomEvent.class), BreweryKey.parse(child)));
            } catch (Exception e) {
                Logger.logErr("Exception when reading custom event: " + child);
                throw e;
            }
        }
        return output;
    }
}
