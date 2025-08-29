package dev.jsinco.brewery.api.event;

import com.google.gson.annotations.SerializedName;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.api.math.RangeD;

import java.util.Map;

public record EventProbability(@SerializedName("probability_weight") ModifierExpression probabilityWeight,
                               @SerializedName("allowed_ranges") Map<String, RangeD> allowedRanges) {

    public static EventProbability NONE = new EventProbability(new ModifierExpression("0"), Map.of());

    public Calculated evaluate(Map<DrunkenModifier, Double> modifiers) {
        for (Map.Entry<String, RangeD> entry : allowedRanges().entrySet()) {
            boolean cancelled = modifiers.keySet().stream().filter(modifier -> modifier.name().equals(entry.getKey()))
                    .findFirst()
                    .filter(modifiers::containsKey)
                    .map(modifiers::get)
                    .map(entry.getValue()::isOutside)
                    .orElse(false);
            if (cancelled) {
                return new Calculated(false, 0D);
            }
        }
        return new Calculated(true, probabilityWeight().evaluate(modifiers));
    }

    public record Calculated(boolean enabled, double probability) {

    }
}
