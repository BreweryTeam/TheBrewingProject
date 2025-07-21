package dev.jsinco.brewery.event;

import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.WeightedProbabilityElement;

public interface DrunkEvent extends WeightedProbabilityElement {

    int alcoholRequirement();

    int toxinsRequirement();

    BreweryKey key();

    String displayName();
}
