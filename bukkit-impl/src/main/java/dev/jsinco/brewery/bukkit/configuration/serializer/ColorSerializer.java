package dev.jsinco.brewery.bukkit.configuration.serializer;

import dev.jsinco.brewery.bukkit.util.color.ColorUtil;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import org.jspecify.annotations.NonNull;

import java.awt.Color;

public class ColorSerializer implements ObjectSerializer<Color> {

    @Override
    public boolean supports(@NonNull Class<?> type) {
        return Color.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull Color object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue(ColorUtil.colorToString(org.bukkit.Color.fromRGB(object.getRGB() & 0xFFFFFF)));
    }

    @Override
    public Color deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String serializedColor = data.getValue(String.class);
        if (serializedColor == null) {
            return null;
        }
        return new Color(ColorUtil.parseColorString(serializedColor).asRGB());
    }
}
