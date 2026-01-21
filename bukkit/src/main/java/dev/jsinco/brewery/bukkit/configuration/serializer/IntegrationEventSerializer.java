package dev.jsinco.brewery.bukkit.configuration.serializer;

import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.meta.MetaDataType;
import dev.jsinco.brewery.api.util.KeyUtil;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.kyori.adventure.key.Key;

import java.util.Set;

public record IntegrationEventSerializer<E extends IntegrationEvent>(
        EventIntegration<E> integration) implements ObjectSerializer<E> {
    @Override
    public boolean supports(@NonNull Class<? super E> type) {
        return integration.eClass().isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull E object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        EventIntegration.EventData eventData = integration.ConvertToData(object);
        Set<Key> keys = eventData.dataKeys();
        if(keys.isEmpty()) {
            data.setValue(eventData.key().minimalized());
            return;
        }
        data.setValue(eventData.key().minimalized() + "{" + keys.stream().map(key -> KeyUtil.minimalize(key) + "=" + eventData.data(key, MetaDataType.STRING)) +"}");
    }

    @Override
    public E deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String serialized = data.getValue(String.class);
        if (serialized == null) {
            return null;
        }
        return integration.convertToEvent(EventIntegration.parseEvent(serialized))
                .orElse(null);
    }
}
