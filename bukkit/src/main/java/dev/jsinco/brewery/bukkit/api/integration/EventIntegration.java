package dev.jsinco.brewery.bukkit.api.integration;

import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.util.BreweryKey;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface EventIntegration<E extends IntegrationEvent> extends Integration {

    Class<E> eClass();

    Optional<E> deserialize(SerializedEvent serializedEvent);

    SerializedEvent serialize(E event);

    record SerializedEvent(BreweryKey key, @Nullable String meta) {
    }
}
