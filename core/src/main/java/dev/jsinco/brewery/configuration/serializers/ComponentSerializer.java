package dev.jsinco.brewery.configuration.serializers;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ComponentSerializer implements ObjectSerializer<Component> {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public boolean supports(@NonNull Class<? super Component> type) {
        return Component.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull Component object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(miniMessage.serialize(object));
    }

    @Override
    public Component deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String serialized = data.getValue(String.class);
        if (serialized == null) {
            return null;
        }
        return miniMessage.deserialize(serialized);
    }
}
