package dev.jsinco.brewery.event;

import dev.jsinco.brewery.util.BreweryKey;

public record CustomEventStep(BreweryKey customEventKey) implements EventStepProperty {
}
