package dev.jsinco.brewery.configuration.serializers;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public class ConsumableSerializer implements ObjectSerializer<ConsumableSerializer.Consumable> {

    public record Consumable(String type, int alcohol, int toxins) {}

    @Override
    public boolean supports(@NonNull Class<? super Consumable> type) {
        return Consumable.class == type;
    }

    @Override
    public void serialize(@NonNull Consumable object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", object.type);
        if (object.alcohol != 0) {
            eventData.put("alcohol", object.alcohol);
        }
        if (object.toxins != 0) {
            eventData.put("toxins", object.toxins);
        }
        data.setValue(eventData);
    }

    @Override
    public Consumable deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String type = data.get("type", String.class);
        Integer alcohol = data.get("alcohol", Integer.class);
        Integer toxins = data.get("toxins", Integer.class);
        if (alcohol == null) {
            alcohol = 0;
        }
        if (toxins == null) {
            toxins = 0;
        }
        return new Consumable(type, alcohol, toxins);
    }

}
