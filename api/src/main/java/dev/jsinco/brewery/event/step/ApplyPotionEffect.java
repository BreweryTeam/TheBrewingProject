package dev.jsinco.brewery.event.step;

import dev.jsinco.brewery.event.EventStepProperty;
import dev.jsinco.brewery.moment.Interval;

public record ApplyPotionEffect(String potionEffectName, Interval amplifierBounds,
                                Interval durationBounds) implements EventStepProperty {
}

