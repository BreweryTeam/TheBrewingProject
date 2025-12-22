package dev.jsinco.brewery.bukkit.meta;

import dev.jsinco.brewery.api.meta.BooleanMetaDataType;
import dev.jsinco.brewery.api.meta.ListMetaDataType;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.persistence.PersistentDataContainerMock;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockBukkitExtension.class)
public class MetaDataPdcTypeTest {

    @ParameterizedTest
    @MethodSource("typedArguments")
    <C> void testWriteAndRead(MetaDataType<?, C> type, C testValue) {
        PersistentDataContainer pdc = new PersistentDataContainerMock();
        MetaData meta = new MetaData()
                .withMeta(Key.key("test", "write_and_read_pdc"), type, testValue);
        NamespacedKey key = new NamespacedKey("test", "meta");

        pdc.set(key, MetaDataPdcType.INSTANCE, meta);
        MetaData retrievedMeta = pdc.get(key, MetaDataPdcType.INSTANCE);
        assertEquals(meta, retrievedMeta);
    }

    // Copied from MetaDataTest

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
    void testListTypeIsNotErased() {
        PersistentDataContainer pdc = new PersistentDataContainerMock();
        Key innerKey = Key.key("test", "list_erasure");
        MetaData meta = new MetaData()
                .withMeta(innerKey, MetaDataType.STRING_LIST, List.of("value"));
        NamespacedKey key = new NamespacedKey("test", "meta");

        pdc.set(key, MetaDataPdcType.INSTANCE, meta);
        MetaData retrievedMeta = pdc.get(key, MetaDataPdcType.INSTANCE);
        assertThrows(IllegalArgumentException.class, () -> retrievedMeta.meta(innerKey, MetaDataType.INTEGER_LIST));
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
