package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.event.CustomEvent;
import dev.jsinco.brewery.api.event.EventStep;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomEventSerializer implements ObjectSerializer<CustomEvent> {
    @Override
    public boolean supports(@NonNull Class<? super CustomEvent> type) {
        return CustomEvent.class == type;
    }

    @Override
    public void serialize(@NonNull CustomEvent object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        Map<String, Object> eventData = new HashMap<>();
        if (object.alcoholRequirement() != 0) {
            eventData.put("alcohol", object.alcoholRequirement());
        }
        if (object.toxinsRequirement() != 0) {
            eventData.put("toxins", object.toxinsRequirement());
        }
        if (object.probabilityWeight() != 0) {
            eventData.put("probability-weight", object.probabilityWeight());
        }
        eventData.put("steps", object.getSteps());
        data.setValue(eventData);
    }

    @Override
    public CustomEvent deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        Integer alcohol = data.get("alcohol", Integer.class);
        Integer toxin = data.get("toxins", Integer.class);
        Integer probabilityWeight = data.get("probability-weight", Integer.class);
        CustomEvent.Builder builder = new CustomEvent.Builder()
                .alcoholRequirement(alcohol == null ? 0 : alcohol)
                .toxinsRequirement(toxin == null ? 0 : toxin)
                .probabilityWeight(probabilityWeight == null ? 0 : probabilityWeight);
        List<EventStep> steps = data.getAsList("steps", EventStep.class);
        Preconditions.checkArgument(steps != null, "Steps has to be a list");
        steps.forEach(builder::addStep);
        return builder.build();
    }
}
