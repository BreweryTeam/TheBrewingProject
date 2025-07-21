package dev.jsinco.brewery.event.step;

import dev.jsinco.brewery.event.EventStepProperty;

public record ConsumeStep(int alcohol, int toxins) implements EventStepProperty {

}