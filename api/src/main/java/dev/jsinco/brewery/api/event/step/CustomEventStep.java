package dev.jsinco.brewery.api.event.step;

import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.util.BreweryKey;

public record CustomEventStep(BreweryKey customEventKey) implements EventStepProperty {
}
