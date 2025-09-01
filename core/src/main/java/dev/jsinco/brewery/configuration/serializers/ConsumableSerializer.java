package dev.jsinco.brewery.configuration.serializers;

import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsumableSerializer implements ObjectSerializer<ConsumableSerializer.Consumable> {

    public record Consumable(String type, Map<DrunkenModifier, Double> modifiers) {
    }

    @Override
    public boolean supports(@NonNull Class<? super Consumable> type) {
        return Consumable.class == type;
    }

    @Override
    public void serialize(@NonNull Consumable object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", object.type);
        object.modifiers.forEach((key, value) -> eventData.put(key.name(), value));
        data.setValue(eventData);
    }

    @Override
    public Consumable deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String type = data.get("type", String.class);
        Map<DrunkenModifier, Double> modifiers = DrunkenModifierSection.modifiers().drunkenModifiers()
                .stream()
                .filter(modifier -> data.containsKey(modifier.name()))
                .collect(Collectors.toUnmodifiableMap(modifier -> modifier,
                        modifier -> data.get(modifier.name(), Double.class))
                );
        return new Consumable(type, modifiers);
    }

}
