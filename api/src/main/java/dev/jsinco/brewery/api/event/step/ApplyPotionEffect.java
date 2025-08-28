package dev.jsinco.brewery.api.event.step;

import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.moment.Interval;

public record ApplyPotionEffect(String potionEffectName, Interval amplifierBounds,
                                Interval durationBounds) implements EventStepProperty {
}

