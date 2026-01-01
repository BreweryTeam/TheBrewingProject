package dev.jsinco.brewery.api.meta;

import net.kyori.adventure.key.Key;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class MetaDataTest {

    @Test
    void testAbsentKey() {
        Key testKey = Key.key("test", "absent");
        MetaData meta = new MetaData();

        String retrievedValue = meta.meta(testKey, MetaDataType.STRING);
        assertNull(retrievedValue, "Retrieved value should be empty");
    }

    @ParameterizedTest
    @MethodSource("typedArguments")
    <C> void testWriteAndRead(MetaDataType<?, C> type, C testValue) {
        Key testKey = Key.key("test", "write_and_read");
        MetaData meta = new MetaData();

        MetaData updatedMeta = meta.withMeta(testKey, type, testValue);

        C retrievedValue = updatedMeta.meta(testKey, type);
        assertEquals(testValue, retrievedValue, "Retrieved value should match the written value");
    }

    private static Stream<Arguments> typedArguments() {
        return Stream.of(
                // Primitives
                Arguments.of(MetaDataType.BYTE, (byte) 3),
                Arguments.of(MetaDataType.INTEGER, 68),
                Arguments.of(MetaDataType.STRING, "value"),
                Arguments.of(MetaDataType.BYTE_ARRAY, "value".getBytes(StandardCharsets.UTF_8)),
                Arguments.of(MetaDataType.CONTAINER, new MetaData()),
                Arguments.of(MetaDataType.CONTAINER, sampleMeta("value")),
                // List primitives
                Arguments.of(MetaDataType.STRING_LIST, Collections.emptyList()),
                Arguments.of(MetaDataType.STRING_LIST, List.of("a", "b", "c")),
                Arguments.of(MetaDataType.INTEGER_LIST, Collections.emptyList()),
                Arguments.of(MetaDataType.INTEGER_LIST, List.of(-1, 0, 1)),
                Arguments.of(MetaDataType.BYTE_ARRAY_LIST, Collections.emptyList()),
                Arguments.of(MetaDataType.BYTE_ARRAY_LIST, List.of("a".getBytes(StandardCharsets.UTF_8), "b".getBytes(StandardCharsets.UTF_8))),
                Arguments.of(MetaDataType.CONTAINER_LIST, Collections.emptyList()),
                Arguments.of(MetaDataType.CONTAINER_LIST, List.of(sampleMeta("value1"), sampleMeta("value2"))),
                // Custom
                Arguments.of(BooleanMetaDataType.INSTANCE, true),
                Arguments.of(ComplexObjectMetaDataType.INSTANCE, new ComplexObject(5, new SimpleObject(7))),
                Arguments.of(ListMetaDataType.from(ComplexObjectMetaDataType.INSTANCE), Collections.emptyList()),
                Arguments.of(ListMetaDataType.from(ComplexObjectMetaDataType.INSTANCE), sampleComplexList())
        );
    }
    private static MetaData sampleMeta(String sampleValue) {
        return new MetaData()
                .withMeta(Key.key("test", "string"), MetaDataType.STRING, sampleValue)
                .withMeta(Key.key("test", "integer"), MetaDataType.INTEGER, 68);
    }
    private static List<ComplexObject> sampleComplexList() {
        return List.of(
                new ComplexObject(5, new SimpleObject(7)),
                new ComplexObject(6, new SimpleObject(8))
        );
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

    private record SimpleObject(int n) {}

    private static class SimpleObjectMetaDataType implements MetaDataType<MetaData, SimpleObject> {

        public static final SimpleObjectMetaDataType INSTANCE = new SimpleObjectMetaDataType();
        private static final Key N = Key.key("test", "n");

        @Override
        public Class<MetaData> getPrimitiveType() {
            return MetaData.class;
        }

        @Override
        public Class<SimpleObject> getComplexType() {
            return SimpleObject.class;
        }

        @Override
        public MetaData toPrimitive(SimpleObject complex) {
            return new MetaData().withMeta(N, MetaDataType.INTEGER, complex.n);
        }

        @Override
        public SimpleObject toComplex(MetaData primitive) {
            return new SimpleObject(
                    primitive.meta(N, MetaDataType.INTEGER)
            );
        }

    }

    private record ComplexObject(int a, SimpleObject o) {}

    private static class ComplexObjectMetaDataType implements MetaDataType<MetaData, ComplexObject> {

        public static final ComplexObjectMetaDataType INSTANCE = new ComplexObjectMetaDataType();
        private static final Key A = Key.key("test", "a");
        private static final Key O = Key.key("test", "o");

        @Override
        public Class<MetaData> getPrimitiveType() {
            return MetaData.class;
        }

        @Override
        public Class<ComplexObject> getComplexType() {
            return ComplexObject.class;
        }

        @Override
        public MetaData toPrimitive(ComplexObject complex) {
            return new MetaData()
                    .withMeta(A, MetaDataType.INTEGER, complex.a)
                    .withMeta(O, SimpleObjectMetaDataType.INSTANCE, complex.o);
        }

        @Override
        public ComplexObject toComplex(MetaData primitive) {
            return new ComplexObject(
                    primitive.meta(A, MetaDataType.INTEGER),
                    primitive.meta(O, SimpleObjectMetaDataType.INSTANCE)
            );
        }

    }

}
