package dev.jsinco.brewery.bukkit.configuration.serializer;

import static org.junit.jupiter.api.Assertions.*;

import dev.jsinco.brewery.bukkit.util.color.ColorUtil;
import eu.okaeri.configs.configurer.InMemoryConfigurer;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerdesContext;
import eu.okaeri.configs.serdes.SerializationData;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.standard.StandardSerdes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

class ColorSerializerTest {

    private static final InMemoryConfigurer configurer = new InMemoryConfigurer();
    private static final ObjectSerializer<Color> serializer = new ColorSerializer();
    private static final SerdesContext serdesContext = SerdesContext.of(configurer);

    static {
        configurer.register(new StandardSerdes());
    }

    @Test
    void supportsColorClass() {
        assertTrue(serializer.supports(Color.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WHITE", "SILVER", "GRAY", "BLACK"})
    void serializesNamedColorsToTheirNames(String name) {
        Color color = new java.awt.Color(ColorUtil.NAME_TO_COLOR_MAP.get(name).asRGB());
        assertNotNull(color);
        SerializationData serializationData = new SerializationData(configurer, serdesContext);

        serializer.serialize(color, serializationData, GenericsDeclaration.of(String.class));

        assertEquals(name, serializationData.asMap().get(ObjectSerializer.VALUE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GOLD", "LIGHT_PURPLE"})
    void serializesNamedTextColorsToTheirNames(String name) {
        Color color = new java.awt.Color(ColorUtil.NAME_TO_COLOR_MAP.get(name).asRGB());
        assertNotNull(color);
        SerializationData serializationData = new SerializationData(configurer, serdesContext);

        serializer.serialize(color, serializationData, GenericsDeclaration.of(String.class));

        assertEquals(name, serializationData.asMap().get(ObjectSerializer.VALUE));
    }

    @Test
    void serializesUnnamedColorsToHexFormat() {
        Color color = new Color(123, 45, 67);
        SerializationData serializationData = new SerializationData(configurer, serdesContext);

        serializer.serialize(color, serializationData, GenericsDeclaration.of(String.class));

        assertEquals("#7b2d43", serializationData.asMap().get(ObjectSerializer.VALUE));
    }

    @Test
    void serializesColorWithZeroValuesCorrectly() {
        Color color = new Color(0, 0, 0);
        SerializationData serializationData = new SerializationData(configurer, serdesContext);

        serializer.serialize(color, serializationData, GenericsDeclaration.of(String.class));

        assertEquals("BLACK", serializationData.asMap().get(ObjectSerializer.VALUE));
    }

    @Test
    void serializesColorWithMaxValuesCorrectly() {
        Color color = new Color(255, 255, 255);
        SerializationData serializationData = new SerializationData(configurer, serdesContext);

        serializer.serialize(color, serializationData, GenericsDeclaration.of(String.class));

        assertEquals("WHITE", serializationData.asMap().get(ObjectSerializer.VALUE));
    }

    @ParameterizedTest
    @CsvSource({"WHITE, WHITE", "RED, RED", "BLUE, BLUE", "GREEN, GREEN", "BLACK, BLACK", "YELLOW, YELLOW"})
    void deserializesNamedColorsCorrectly(String input, String expectedName) {
        Map<String, Object> map = new HashMap<>();
        map.put(ObjectSerializer.VALUE, input);
        DeserializationData deserializationData = new DeserializationData(map, configurer, serdesContext);

        Color color = serializer.deserialize(deserializationData, GenericsDeclaration.of(String.class));

        assertNotNull(color);
        Color expectedColor = new java.awt.Color(ColorUtil.NAME_TO_COLOR_MAP.get(expectedName).asRGB());
        assertNotNull(expectedColor);
        assertEquals(expectedColor.getRGB() & 0xFFFFFF, color.getRGB() & 0xFFFFFF);
    }

    @ParameterizedTest
    @CsvSource({"GOLD, GOLD", "LIGHT_PURPLE, LIGHT_PURPLE"})
    void deserializesNamedTextColorsCorrectly(String input, String expectedName) {
        Map<String, Object> map = new HashMap<>();
        map.put(ObjectSerializer.VALUE, input);
        DeserializationData deserializationData = new DeserializationData(map, configurer, serdesContext);

        Color color = serializer.deserialize(deserializationData, GenericsDeclaration.of(String.class));

        assertNotNull(color);
        Color expectedColor = new java.awt.Color(ColorUtil.NAME_TO_COLOR_MAP.get(expectedName).asRGB());
        assertNotNull(expectedColor);
        assertEquals(expectedColor.getRGB() & 0xFFFFFF, color.getRGB() & 0xFFFFFF);
    }

    @ParameterizedTest
    @CsvSource({"#FF0000, FF0000", "#00FF00, 00FF00", "#0000FF, 0000FF", "#7B2D43, 7B2D43", "#ABCDEF, ABCDEF"})
    void deserializesHexColorsCorrectly(String input, String expectedHex) {
        Map<String, Object> map = new HashMap<>();
        map.put(ObjectSerializer.VALUE, input);
        DeserializationData deserializationData = new DeserializationData(map, configurer, serdesContext);

        Color color = serializer.deserialize(deserializationData, GenericsDeclaration.of(String.class));

        assertNotNull(color);
        int expectedRgb = Integer.parseInt(expectedHex, 16);
        assertEquals(expectedRgb, color.getRGB() & 0xFFFFFF);
    }

    @ParameterizedTest
    @ValueSource(strings = {"#ff0000", "#00ff00", "#0000ff", "#abcdef", "#ABCDEF", "FF0000", "00FF00"})
    void deserializesCaseInsensitiveHexColors(String input) {
        Map<String, Object> map = new HashMap<>();
        map.put(ObjectSerializer.VALUE, input);
        DeserializationData deserializationData = new DeserializationData(map, configurer, serdesContext);

        Color color = serializer.deserialize(deserializationData, GenericsDeclaration.of(String.class));

        assertNotNull(color);
    }

    @Test
    void deserializesNullValueReturnsNull() {
        Map<String, Object> map = new HashMap<>();
        map.put(ObjectSerializer.VALUE, null);
        DeserializationData deserializationData = new DeserializationData(map, configurer, serdesContext);

        Color color = serializer.deserialize(deserializationData, GenericsDeclaration.of(String.class));

        assertNull(color);
    }

    @Test
    void deserializesEmptyStringThrows() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", "");
        DeserializationData deserializationData = new DeserializationData(map, configurer, serdesContext);

        assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(deserializationData, GenericsDeclaration.of(String.class)));
    }

    @Test
    void deserializesInvalidStringThrows() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", "#1234567");
        DeserializationData deserializationData = new DeserializationData(map, configurer, serdesContext);

        assertThrows(IllegalArgumentException.class, () -> serializer.deserialize(deserializationData, GenericsDeclaration.of(String.class)));
    }
}