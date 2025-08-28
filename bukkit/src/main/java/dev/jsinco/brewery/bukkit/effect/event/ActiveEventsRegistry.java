package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.api.event.NamedDrunkEvent;
import dev.jsinco.brewery.api.util.BreweryKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Potential memory leak, probably not that large though
public class ActiveEventsRegistry {

    private Map<UUID, Map<BreweryKey, Long>> events = new HashMap<>();

    public boolean hasActiveEvent(UUID playerUuid, NamedDrunkEvent event) {
        Map<BreweryKey, Long> playerEvents = events.get(playerUuid);
        if (playerEvents == null) {
            return false;
        }
        BreweryKey key = event.key();
        return playerEvents.containsKey(key) && playerEvents.get(key) > TheBrewingProject.getInstance().getTime();
    }

    public void registerActiveEvent(UUID playerUuid, NamedDrunkEvent event, int duration) {
        events.computeIfAbsent(playerUuid, ignored -> new HashMap<>())
                .put(event.key(), TheBrewingProject.getInstance().getTime() + duration);
    }
}
