package dev.jsinco.brewery.api.event;

import com.google.gson.annotations.SerializedName;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.api.math.RangeD;

import java.util.Map;

public record EventProbability(@SerializedName("probability_weight") ModifierExpression probabilityWeight,
                               @SerializedName("allowed_ranges") Map<String, RangeD> allowedRanges) {

    public static EventProbability NONE = new EventProbability(new ModifierExpression("0"), Map.of());
}
