package dev.jsinco.brewery.configuration.serializers;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsumableSerializer implements ObjectSerializer<ConsumableSerializer.Consumable> {

    public record Consumable(String type, Map<String, Double> modifiers) {
    }

    @Override
    public boolean supports(@NonNull Class<? super Consumable> type) {
        return Consumable.class == type;
    }

    @Override
    public void serialize(@NonNull Consumable object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", object.type);
        eventData.putAll(object.modifiers);
        data.setValue(eventData);
    }

    @Override
    public Consumable deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String type = data.get("type", String.class);
        return new Consumable(type, data.asMap().keySet()
                .stream()
                .filter(key -> !key.equals("type"))
                .collect(Collectors.toUnmodifiableMap(key -> key, key -> data.get(key, Double.class)))
        );
    }

}
