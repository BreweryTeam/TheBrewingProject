package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.api.event.EventProbability;
import dev.jsinco.brewery.api.event.NamedDrunkEvent;
import dev.jsinco.brewery.api.math.RangeD;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

public class NamedDrunkEventSerializer implements ObjectSerializer<NamedDrunkEvent> {
    @Override
    public boolean supports(@NonNull Class<? super NamedDrunkEvent> type) {
        return type == NamedDrunkEvent.class;
    }

    @Override
    public void serialize(@NonNull NamedDrunkEvent object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("name", object.key().key());
        if (object.probability() != EventProbability.NONE) {
            data.add("probability", object.probability());
        }
    }

    @Override
    public NamedDrunkEvent deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String name = data.get("name", String.class);
        EventProbability probability;
        if ((data.containsKey("alcohol-requirement") || data.containsKey("toxins-requirement")) && data.containsKey("probability-weight")) {
            ImmutableMap.Builder<String, RangeD> ranges = new ImmutableMap.Builder<>();
            if (data.containsKey("alcohol-requirement")) {
                ranges.put("alcohol", new RangeD(data.get("alcohol-requirement", Double.class), null));
            }
            if (data.containsKey("toxins-requirement")) {
                ranges.put("toxins", new RangeD(data.get("toxins-requirement", Double.class), null));
            }
            probability = new EventProbability(
                    new ModifierExpression(data.get("probability-weight", Double.class) / 5 + "*probabilityWeight(alcohol)"),
                    ranges.build()
            );
        } else {
            probability = data.get("probability", EventProbability.class);
        }
        Preconditions.checkArgument(name != null, "Unknown event type, missing name key");
        return new NamedDrunkEvent(name, probability == null ? EventProbability.NONE : probability);
    }
}
