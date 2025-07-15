package dev.jsinco.brewery.event;

import dev.jsinco.brewery.vector.BreweryLocation;

import java.util.function.Supplier;

public record Teleport(Supplier<BreweryLocation> location) implements EventStep {
}
