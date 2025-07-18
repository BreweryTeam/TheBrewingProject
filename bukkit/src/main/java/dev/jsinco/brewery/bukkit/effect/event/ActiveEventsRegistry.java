package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.event.named.NamedDrunkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Potential memory leak, probably not that large though
public class ActiveEventsRegistry {

    private Map<UUID, Map<Class<? extends NamedDrunkEvent>, Long>> events = new HashMap<>();

    public boolean hasActiveEvent(UUID playerUuid, Class<? extends NamedDrunkEvent> event) {
        Map<Class<? extends NamedDrunkEvent>, Long> playerEvents = events.get(playerUuid);
        if (playerEvents == null) {
            return false;
        }
        return playerEvents.containsKey(event) && playerEvents.get(event) > TheBrewingProject.getInstance().getTime();
    }

    public void registerActiveEvent(UUID playerUuid, Class<? extends NamedDrunkEvent> event, int duration) {
        events.computeIfAbsent(playerUuid, ignored -> new HashMap<>())
                .put(event, TheBrewingProject.getInstance().getTime() + duration);
    }
}
