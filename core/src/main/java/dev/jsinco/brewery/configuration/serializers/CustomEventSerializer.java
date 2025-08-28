package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.event.CustomEvent;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.EventProbability;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.List;

public class CustomEventSerializer implements ObjectSerializer<CustomEvent> {
    @Override
    public boolean supports(@NonNull Class<? super CustomEvent> type) {
        return CustomEvent.class == type;
    }

    @Override
    public void serialize(@NonNull CustomEvent object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        if (object.probability() != EventProbability.NONE) {
            data.add("probability", object.probability());
        }
        data.add("steps", object.getSteps());
    }

    @Override
    public CustomEvent deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        EventProbability probability = data.get("probability", EventProbability.class);
        CustomEvent.Builder builder = new CustomEvent.Builder()
                .probability(probability == null ? EventProbability.NONE : probability);
        List<EventStep> steps = data.getAsList("steps", EventStep.class);
        Preconditions.checkArgument(steps != null, "Steps has to be a list");
        steps.forEach(builder::addStep);
        return builder.build();
    }
}
