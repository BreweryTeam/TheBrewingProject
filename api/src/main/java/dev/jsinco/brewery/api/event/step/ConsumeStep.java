package dev.jsinco.brewery.api.event.step;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.event.EventStepProperty;

import java.util.Map;

public record ConsumeStep(Map<DrunkenModifier, Double> modifiers) implements EventStepProperty {

}