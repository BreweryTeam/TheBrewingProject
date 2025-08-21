package dev.jsinco.brewery.event.step;

import dev.jsinco.brewery.event.EventStepProperty;
import dev.jsinco.brewery.vector.BreweryLocation;

public record Teleport(BreweryLocation.Uncompiled location) implements EventStepProperty {
}
