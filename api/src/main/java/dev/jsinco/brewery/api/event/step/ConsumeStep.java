package dev.jsinco.brewery.api.event.step;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.event.EventStepProperty;

public record ConsumeStep(DrunkenModifier modifier, double incrementValue) implements EventStepProperty {

}