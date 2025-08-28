package dev.jsinco.brewery.configuration.serializers;

import com.google.common.base.Preconditions;
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
        data.add("alcohol-requirement", object.alcoholRequirement());
        data.add("toxins-requirement", object.toxinsRequirement());
        data.add("probability-weight", object.probabilityWeight());
    }

    @Override
    public NamedDrunkEvent deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String name = data.get("name", String.class);
        Integer alcoholRequirement = data.get("alcohol-requirement", Integer.class);
        Integer toxinsRequirement = data.get("toxins-requirement", Integer.class);
        Integer probabilityWeight = data.get("probability-weight", Integer.class);
        Preconditions.checkArgument(name != null, "Unknown event type, missing name key");
        return new NamedDrunkEvent(name, alcoholRequirement == null ? 0 : alcoholRequirement, toxinsRequirement == null ? 0 : toxinsRequirement, probabilityWeight == null ? 0 : probabilityWeight);
    }
}
