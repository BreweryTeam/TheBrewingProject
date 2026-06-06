package dev.jsinco.brewery.bukkit.api.effect;

import dev.jsinco.brewery.api.event.DrunkEvent;

import java.util.Collection;
import java.util.UUID;

public interface DrunkEventManager {

    /**
     * @return All drunken events loaded by this plugin
     */
    Collection<DrunkEvent> allEvents();

    /**
     * Run an event
     *
     * @param playerUuid The uuid of player to run the event on
     * @param event      The event to run
     */
    void runEvent(UUID playerUuid, DrunkEvent event);
}
