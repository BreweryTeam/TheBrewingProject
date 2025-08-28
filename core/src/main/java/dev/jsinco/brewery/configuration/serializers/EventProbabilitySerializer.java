package dev.jsinco.brewery.configuration.serializers;

import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.api.event.EventProbability;
import dev.jsinco.brewery.api.math.RangeD;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.Map;

public class EventProbabilitySerializer implements ObjectSerializer<EventProbability> {
    @Override
    public boolean supports(@NonNull Class<? super EventProbability> type) {
        return EventProbability.class == type;
    }

    @Override
    public void serialize(@NonNull EventProbability object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        if (object.probabilityWeight() != ModifierExpression.ZERO) {
            data.add("probability-weight", object.probabilityWeight());
        }
        if (!object.allowedRanges().isEmpty()) {
            data.add("allowed-ranges", object.allowedRanges());
        }
    }

    @Override
    public EventProbability deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        ModifierExpression expression = data.get("probability-weight", ModifierExpression.class);
        Map<String, RangeD> allowedRanges = data.getAsMap("allowed-ranges", String.class, RangeD.class);
        return new EventProbability(
                expression == null ? ModifierExpression.ZERO : expression,
                allowedRanges == null ? Map.of() : allowedRanges
        );
    }
}
