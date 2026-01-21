package dev.jsinco.brewery.bukkit.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import dev.jsinco.brewery.api.event.DrunkEvent;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.EventIntegration;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EventUtil {

    public static Optional<DrunkEvent> fromKey(BreweryKey key) {
        TheBrewingProject theBrewingProject = TheBrewingProject.getInstance();
        return Optional.<DrunkEvent>ofNullable(BreweryRegistry.DRUNK_EVENT.get(key))
                .or(() -> Optional.ofNullable(theBrewingProject.getCustomDrunkEventRegistry().getCustomEvent(key)))
                .or(() -> {
                            EventIntegration.EventData serialized = EventIntegration.parseEvent(key.toString());
                            return theBrewingProject.getIntegrationManager().getIntegrationRegistry()
                                    .getIntegrations(IntegrationTypes.EVENT)
                                    .stream()
                                    .map(eventIntegration -> eventIntegration.convertToEvent(serialized))
                                    .flatMap(Optional::stream)
                                    .findFirst();
                        }
                );
    }

    public static List<BreweryKey> listAll() {
        ImmutableList.Builder<BreweryKey> keyBuilder = ImmutableList.builder();
        Streams.concat(TheBrewingProject.getInstance().getCustomDrunkEventRegistry().events().stream(), BreweryRegistry.DRUNK_EVENT.values().stream())
                .map(DrunkEvent::key)
                .forEach(keyBuilder::add);
        TheBrewingProject.getInstance().getIntegrationManager().getIntegrationRegistry()
                .getIntegrations(IntegrationTypes.EVENT)
                .stream()
                .map(EventIntegration::listEventKeys)
                .flatMap(Collection::stream)
                .forEach(keyBuilder::add);
        return keyBuilder.build();
    }
}
