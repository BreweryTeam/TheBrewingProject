package dev.jsinco.brewery.bukkit.configuration.serializer;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.kyori.adventure.text.format.NamedTextColor;

import java.awt.*;
import java.util.HexFormat;

public class ColorSerializer implements ObjectSerializer<Color> {

    private static final HexFormat EXPLICIT_HEX_FORMAT = HexFormat.of().withPrefix("#");
    private static final HexFormat IMPLICIT_HEX_FORMAT = HexFormat.of();

    @Override
    public boolean supports(@NonNull Class<? super Color> type) {
        return Color.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull Color object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        int rgb = object.getRGB() & 0xFFFFFF;
        for (NamedTextColor textColor : NamedTextColor.NAMES.values()) {
            if ((textColor.value() & 0xFFFFFF) == rgb) {
                data.setValue(textColor.toString());
                return;
            }
        }
        data.setValue(EXPLICIT_HEX_FORMAT.toHexDigits(rgb, 6));
    }

    @Override
    public Color deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String serializedColor = data.getValue(String.class);
        if(serializedColor == null){
            return null;
        }
        if(serializedColor.startsWith("#")) {
            return new Color(HexFormat.fromHexDigits(serializedColor, 1, 7));
        }
        for(NamedTextColor textColor : NamedTextColor.NAMES.values()){
            if(textColor.toString().equalsIgnoreCase(serializedColor)) {
                return new Color(textColor.value());
            }
        }
        return new Color(HexFormat.fromHexDigits(serializedColor));
    }
}
