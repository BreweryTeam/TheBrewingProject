package dev.jsinco.brewery.api.event.step;

import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.vector.BreweryLocation;

public record Teleport(BreweryLocation.Uncompiled location) implements EventStepProperty {
}
