package dev.jsinco.brewery.event;

import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.WeightedProbabilityElement;

public sealed interface DrunkEvent extends WeightedProbabilityElement, EventStep permits CustomEvent, NamedDrunkEvent {

    int alcoholRequirement();

    int toxinsRequirement();

    BreweryKey key();

    String displayName();
}
