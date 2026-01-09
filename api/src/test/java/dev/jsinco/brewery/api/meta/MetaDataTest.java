package dev.jsinco.brewery.api.meta;

import net.kyori.adventure.key.Key;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MetaDataTest {

    @Test
    void testAbsentKey() {
        Key testKey = Key.key("test", "absent");
        MetaData meta = new MetaData();

        String retrievedValue = meta.meta(testKey, MetaDataType.STRING);
        assertNull(retrievedValue, "Retrieved value should be empty");
    }

    @ParameterizedTest(name = "{index} ==> write and read with type {0} and value {1}")
    @MethodSource("dev.jsinco.brewery.api.meta.MetaDataSamples#metaDataSampleProvider")
    <C> void testWriteAndRead(MetaDataType<?, C> type, C testValue) {
        Key testKey = Key.key("test", "write_and_read");
        MetaData meta = new MetaData();

        MetaData updatedMeta = meta.withMeta(testKey, type, testValue);

        C retrievedValue = updatedMeta.meta(testKey, type);
        assertEquals(testValue, retrievedValue, "Retrieved value should match the written value");
    }

    @Test
    void testWrongDataType() {
        Key testKey = Key.key("test", "string");
        MetaData meta = new MetaData();

        MetaData updatedMeta = meta.withMeta(testKey, MetaDataType.STRING, "value");

        assertThrows(IllegalArgumentException.class, () -> updatedMeta.meta(testKey, MetaDataType.INTEGER));
    }

    @Test
    void testWrongListDataType() {
        Key testKey = Key.key("test", "string_list");
        MetaData meta = new MetaData();

        MetaData updatedMeta = meta.withMeta(testKey, MetaDataType.STRING_LIST, List.of("value"));

        assertThrows(IllegalArgumentException.class, () -> updatedMeta.meta(testKey, MetaDataType.INTEGER_LIST));
    }

    @Test
    void testWithoutMeta() {
        Key testKey = Key.key("test", "string");
        MetaData meta = new MetaData().withMeta(testKey, MetaDataType.STRING, "value");

        MetaData withoutMeta = meta.withoutMeta(testKey);

        assertNull(withoutMeta.meta(testKey, MetaDataType.STRING));
    }

    @Test
    void testArrayEqualsJank() {
        // array.equals() uses identity comparison, which can cause problems in .equals() calls
        Key testKey = Key.key("test", "array_equals");
        MetaData meta1 = new MetaData().withMeta(testKey, MetaDataType.BYTE_ARRAY, new byte[] { 1, 2, 3 });
        MetaData meta2 = new MetaData().withMeta(testKey, MetaDataType.BYTE_ARRAY, new byte[] { 1, 2, 3 });
        assertEquals(meta1, meta2);
        assertEquals(meta2, meta1);
    }

}
