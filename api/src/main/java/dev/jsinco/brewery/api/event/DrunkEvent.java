package dev.jsinco.brewery.api.event;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.math.RangeD;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.WeightedProbabilityElement;
import net.kyori.adventure.text.Component;

import java.util.Map;


public interface DrunkEvent {
    BreweryKey key();

    Component displayName();

    EventProbability probability();

    default Compiled compile(Map<DrunkenModifier, Double> modifiers) {
        EventProbability probability = probability();
        for (Map.Entry<String, RangeD> entry : probability.allowedRanges().entrySet()) {
            boolean cancelled = modifiers.keySet().stream().filter(modifier -> modifier.name().equals(entry.getKey()))
                    .findFirst()
                    .filter(modifiers::containsKey)
                    .map(modifiers::get)
                    .map(entry.getValue()::isOutside)
                    .orElse(false);
            if (cancelled) {
                return new Compiled(this, false, 0D);
            }
        }
        return new Compiled(this, true, probability.probabilityWeight().evaluate(modifiers));
    }

    record Compiled(DrunkEvent drunkEvent, boolean enabled,
                    double probabilityWeight) implements WeightedProbabilityElement {

    }
}
