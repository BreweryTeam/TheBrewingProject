package dev.jsinco.brewery.api.event.step;

import dev.jsinco.brewery.api.event.EventStepProperty;

public record ConditionalStep(Condition condition) implements EventStepProperty {
}
