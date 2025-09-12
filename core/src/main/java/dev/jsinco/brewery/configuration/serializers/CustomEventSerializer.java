package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.api.event.CustomEvent;
import dev.jsinco.brewery.api.event.EventProbability;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.math.RangeD;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.kyori.adventure.text.Component;

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
        if (!object.displayName().equals(Component.text("?"))) {
            data.add("display-name", object.displayName());
        }
        data.add("steps", object.getSteps());
    }

    @Override
    public CustomEvent deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        EventProbability probability;
        if ((data.containsKey("alcohol") || data.containsKey("toxins")) && data.containsKey("probability-weight")) {
            ImmutableMap.Builder<String, RangeD> ranges = new ImmutableMap.Builder<>();
            if (data.containsKey("alcohol")) {
                ranges.put("alcohol", new RangeD(data.get("alcohol", Double.class), null));
            }
            if (data.containsKey("toxins")) {
                ranges.put("toxins", new RangeD(data.get("toxins", Double.class), null));
            }
            probability = new EventProbability(
                    new ModifierExpression(data.get("probability-weight", Double.class) / 5 + "*probabilityWeight(alcohol)"),
                    ranges.build()
            );
        } else {
            probability = data.get("probability", EventProbability.class);
        }
        CustomEvent.Builder builder = new CustomEvent.Builder()
                .probability(probability == null ? EventProbability.NONE : probability);

        List<EventStep> steps = data.getAsList("steps", EventStep.class);
        Component displayName = data.get("display-name", Component.class);
        if (displayName != null) {
            builder.displayName(displayName);
        }
        Preconditions.checkArgument(steps != null, "Steps has to be a list");
        steps.forEach(builder::addStep);
        return builder.build();
    }
}
