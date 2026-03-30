package dev.jsinco.brewery.api.event.step;

import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.util.BreweryKey;

public record CustomEventCompleted(BreweryKey eventKey) implements EventStepProperty {
}
