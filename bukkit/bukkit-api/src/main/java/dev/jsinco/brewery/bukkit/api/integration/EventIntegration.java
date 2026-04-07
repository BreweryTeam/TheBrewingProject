package dev.jsinco.brewery.bukkit.api.integration;

import dev.jsinco.brewery.api.event.EventData;
import dev.jsinco.brewery.api.event.IntegrationEvent;
import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.util.BreweryKey;

import java.util.List;
import java.util.Optional;

public interface EventIntegration<E extends IntegrationEvent> extends Integration {

    Class<E> eClass();

    List<BreweryKey> listEventKeys();

    Optional<E> convertToEvent(EventData eventData);

    EventData convertToData(E event);

}
