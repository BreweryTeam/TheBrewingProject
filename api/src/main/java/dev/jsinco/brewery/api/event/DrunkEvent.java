package dev.jsinco.brewery.api.event;

import dev.jsinco.brewery.api.util.BreweryKey;
import net.kyori.adventure.text.Component;


public interface DrunkEvent {
    BreweryKey key();

    Component displayName();

    EventProbability probability();
}
