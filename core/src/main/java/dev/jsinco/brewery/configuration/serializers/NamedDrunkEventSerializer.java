package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.event.EventProbability;
import dev.jsinco.brewery.api.event.NamedDrunkEvent;
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
        EventProbability probability = data.get("probability", EventProbability.class);
        Preconditions.checkArgument(name != null, "Unknown event type, missing name key");
        return new NamedDrunkEvent(name, probability == null ? EventProbability.NONE : probability);
    }
}
