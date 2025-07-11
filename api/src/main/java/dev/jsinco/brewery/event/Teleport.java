package dev.jsinco.brewery.event;

import dev.jsinco.brewery.vector.BreweryLocation;

public record Teleport(BreweryLocation location) implements EventStep {
}
