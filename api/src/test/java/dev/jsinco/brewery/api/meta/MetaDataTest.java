package dev.jsinco.brewery.api.meta;

import net.kyori.adventure.key.Key;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;

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
    void testOverwriteKey() {
        Key testKey = Key.key("test", "overwrite");
        MetaData meta = new MetaData()
                .withMeta(testKey, MetaDataType.STRING, "first")
                .withMeta(testKey, MetaDataType.STRING, "second");
        assertEquals("second", meta.meta(testKey, MetaDataType.STRING));
    }

    @Test
    void testOverwriteKeyDifferentType() {
        Key testKey = Key.key("test", "overwrite");
        MetaData meta = new MetaData()
                .withMeta(testKey, MetaDataType.STRING, "first")
                .withMeta(testKey, MetaDataType.INTEGER, 2);
        assertEquals(2, meta.meta(testKey, MetaDataType.INTEGER));
    }

    @Test
    void testWithoutMeta() {
        Key testKey = Key.key("test", "string");
        MetaData meta = new MetaData().withMeta(testKey, MetaDataType.STRING, "value");

        MetaData withoutMeta = meta.withoutMeta(testKey);

        assertNull(withoutMeta.meta(testKey, MetaDataType.STRING));
    }

    @Test
    void testHasMetaNothing() {
        Key testKey = Key.key("test", "nothing");
        MetaData meta = new MetaData();

        assertFalse(meta.hasMeta(testKey, MetaDataType.STRING));
    }

    @Test
    void testHasMetaSomething() {
        Key testKey = Key.key("test", "string");
        MetaData meta = new MetaData().withMeta(testKey, MetaDataType.STRING, "example");

        assertTrue(meta.hasMeta(testKey, MetaDataType.STRING));
        assertFalse(meta.hasMeta(testKey, MetaDataType.INTEGER));
        assertFalse(meta.hasMeta(testKey, MetaDataType.BYTE_ARRAY));
        assertFalse(meta.hasMeta(testKey, MetaDataType.STRING_LIST));
    }

    @Test
    void testMetaKeys() {
        Key key1 = Key.key("test", "one");
        Key key2 = Key.key("test", "two");
        Key key3 = Key.key("test", "three");
        MetaData meta = new MetaData()
                .withMeta(key1, MetaDataType.STRING, "a")
                .withMeta(key2, MetaDataType.STRING, "b")
                .withMeta(key3, MetaDataType.STRING, "c");

        assertEquals(Set.of(key1, key2, key3), meta.metaKeys());
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
