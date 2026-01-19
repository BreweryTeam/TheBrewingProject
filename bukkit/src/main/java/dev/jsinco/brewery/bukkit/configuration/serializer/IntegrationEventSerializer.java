package dev.jsinco.brewery.bukkit.configuration.serializer;

import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record IntegrationEventSerializer<E extends IntegrationEvent>(
        EventIntegration<E> integration) implements ObjectSerializer<E> {

    private static final Pattern EVENT_META_GREEDY_RE = Pattern.compile("([^{}]+)\\{(.*)}]");

    @Override
    public boolean supports(@NonNull Class<? super E> type) {
        return integration.eClass().isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull E object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        EventIntegration.SerializedEvent serializedEvent = integration.serialize(object);
        data.setValue(serializedEvent.key().minimalized() + (serializedEvent.meta() == null ? "" : "{" + serializedEvent.meta() + "}"));
    }

    @Override
    public E deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String serialized = data.getValue(String.class);
        if (serialized == null) {
            return null;
        }
        Matcher matcher = EVENT_META_GREEDY_RE.matcher(serialized);
        BreweryKey key;
        String meta;
        if (matcher.matches()) {
            String group2 = matcher.group(2);
            meta = group2.isBlank() ? null : group2;
            key = BreweryKey.parse(matcher.group(1));
        } else {
            key = BreweryKey.parse(serialized);
            meta = null;
        }
        return integration.deserialize(new EventIntegration.SerializedEvent(key, meta))
                .orElse(null);
    }
}
