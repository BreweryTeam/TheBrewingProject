package dev.jsinco.brewery.api.event.step;

import dev.jsinco.brewery.api.event.EventStepProperty;

public record ConsumeStep(int alcohol, int toxins) implements EventStepProperty {

}