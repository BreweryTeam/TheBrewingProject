package dev.jsinco.brewery.api.event;

import com.google.gson.annotations.SerializedName;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.api.math.RangeD;

import java.util.Map;

public record EventProbability(@SerializedName("probability_expression") ModifierExpression probabilityExpression,
                               @SerializedName("allowed_ranges") Map<String, RangeD> allowedRanges) {

    public static EventProbability NONE = new EventProbability(new ModifierExpression("0"), Map.of());

    public Calculated evaluate(Map<String, Double> variables) {
        for (Map.Entry<String, RangeD> entry : allowedRanges().entrySet()) {
            boolean cancelled = variables.keySet().stream().filter(modifier -> modifier.equals(entry.getKey()))
                    .findFirst()
                    .filter(variables::containsKey)
                    .map(variables::get)
                    .map(entry.getValue()::isOutside)
                    .orElse(false);
            if (cancelled) {
                return new Calculated(false, 0D);
            }
        }
        return new Calculated(true, probabilityExpression().evaluate(variables));
    }

    public record Calculated(boolean enabled, double probability) {

    }
}
