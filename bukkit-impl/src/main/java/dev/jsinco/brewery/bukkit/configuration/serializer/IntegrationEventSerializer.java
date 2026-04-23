package dev.jsinco.brewery.bukkit.configuration.serializer;

import dev.jsinco.brewery.api.event.EventData;
import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.integration.IntegrationRegistry;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jspecify.annotations.NonNull;

public record IntegrationEventSerializer(
        IntegrationRegistry integrations) implements ObjectSerializer<IntegrationEvent> {
    @Override
    public boolean supports(@NonNull Class<?> type) {
        return IntegrationEvent.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull IntegrationEvent object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        for (EventIntegration<?> integration : integrations.getIntegrations(IntegrationTypes.EVENT)) {
            if (setDataIfMatches(integration, data, object)) {
                return;
            }
        }
    }

    private <T extends IntegrationEvent> boolean setDataIfMatches(EventIntegration<T> integration, SerializationData data, IntegrationEvent object) {
        if (integration.eClass().isInstance(object)) {
            EventData eventData = integration.convertToData(integration.eClass().cast(object));
            data.setValue(eventData.serialized());
            return true;
        }
        return false;
    }

    @Override
    public IntegrationEvent deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String serialized = data.getValue(String.class);
        if (serialized == null) {
            return null;
        }
        EventData eventData = EventData.deserialize(serialized);
        for (EventIntegration<?> eventIntegration : integrations.getIntegrations(IntegrationTypes.EVENT)) {
            if (eventData.key().namespace().equals(eventIntegration.getId())) {
                return eventIntegration.convertToEvent(eventData)
                        .orElse(null);
            }
        }
        return null;
    }
}
