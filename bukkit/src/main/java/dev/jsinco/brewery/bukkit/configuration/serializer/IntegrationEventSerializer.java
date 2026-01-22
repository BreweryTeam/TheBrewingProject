package dev.jsinco.brewery.bukkit.configuration.serializer;

import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.event.EventData;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public record IntegrationEventSerializer<E extends IntegrationEvent>(
        EventIntegration<E> integration) implements ObjectSerializer<E> {
    @Override
    public boolean supports(@NonNull Class<? super E> type) {
        return integration.eClass().isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull E object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        EventData eventData = integration.convertToData(object);
        data.setValue(eventData.serialized());
    }

    @Override
    public E deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String serialized = data.getValue(String.class);
        if (serialized == null) {
            return null;
        }
        return integration.convertToEvent(EventData.deserialize(serialized))
                .orElse(null);
    }
}
