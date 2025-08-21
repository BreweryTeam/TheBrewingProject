package dev.jsinco.brewery.event.step;

import dev.jsinco.brewery.event.EventStepProperty;
import dev.jsinco.brewery.util.BreweryKey;

public record CustomEventStep(BreweryKey customEventKey) implements EventStepProperty {
}
