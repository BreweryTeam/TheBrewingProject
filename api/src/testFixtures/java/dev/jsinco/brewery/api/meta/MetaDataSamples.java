package dev.jsinco.brewery.api.meta;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Named.named;

public class MetaDataSamples {

    /**
     * @return A stream of arguments, each containing a MetaDataType and a sample value of that type.
     */
    public static Stream<Arguments> metaDataSampleProvider() {
        return Stream.of(
                // Primitives
                Arguments.of(named("Byte", MetaDataType.BYTE), (byte) 3),
                Arguments.of(named("Integer", MetaDataType.INTEGER), 68),
                Arguments.of(named("String", MetaDataType.STRING), "value"),
                Arguments.of(named("ByteArray", MetaDataType.BYTE_ARRAY), "value".getBytes(StandardCharsets.UTF_8)),
                Arguments.of(named("Container", MetaDataType.CONTAINER), new MetaData()),
                Arguments.of(named("Container", MetaDataType.CONTAINER), sampleMeta("value")),
                // List primitives
                Arguments.of(named("StringList", MetaDataType.STRING_LIST), Collections.emptyList()),
                Arguments.of(named("StringList", MetaDataType.STRING_LIST), List.of("a", "b", "c")),
                Arguments.of(named("IntegerList", MetaDataType.INTEGER_LIST), Collections.emptyList()),
                Arguments.of(named("IntegerList", MetaDataType.INTEGER_LIST), List.of(-1, 0, 1)),
                Arguments.of(named("ByteArrayList", MetaDataType.BYTE_ARRAY_LIST), Collections.emptyList()),
                Arguments.of(named("ByteArrayList", MetaDataType.BYTE_ARRAY_LIST), List.of("a".getBytes(StandardCharsets.UTF_8), "b".getBytes(StandardCharsets.UTF_8))),
                Arguments.of(named("ContainerList", MetaDataType.CONTAINER_LIST), Collections.emptyList()),
                Arguments.of(named("ContainerList", MetaDataType.CONTAINER_LIST), List.of(sampleMeta("value1"), sampleMeta("value2"))),
                // Nested list
                Arguments.of(named("StringListList", ListMetaDataType.from(MetaDataType.STRING_LIST)), List.of()),
                Arguments.of(named("StringListList", ListMetaDataType.from(MetaDataType.STRING_LIST)), List.of(List.of("a", "b", "c"), List.of())),
                Arguments.of(named("DeeplyNestedStringList", deeplyNestedListType()), deeplyNestedList()),
                // Custom
                Arguments.of(named("Boolean", BooleanMetaDataType.INSTANCE), true),
                Arguments.of(named("ComplexObject", ComplexObjectMetaDataType.INSTANCE), new ComplexObject(5, new SimpleObject(7))),
                Arguments.of(named("ComplexObjectList", ListMetaDataType.from(ComplexObjectMetaDataType.INSTANCE)), Collections.emptyList()),
                Arguments.of(named("ComplexObjectList", ListMetaDataType.from(ComplexObjectMetaDataType.INSTANCE)), sampleComplexList())
        );
    }
    private static MetaData sampleMeta(String sampleValue) {
        return new MetaData()
                .withMeta(Key.key("test", "string"), MetaDataType.STRING, sampleValue)
                .withMeta(Key.key("test", "integer"), MetaDataType.INTEGER, 68);
    }
    private static Object deeplyNestedList() {
        Object list = "why";
        for (int i = 0; i < ListMetaDataType.MAX_DEPTH; i++) {
            list = List.of(list);
        }
        return list;
    }
    private static MetaDataType<?, ?> deeplyNestedListType() {
        MetaDataType<?, ?> type = MetaDataType.STRING;
        for (int i = 0; i < ListMetaDataType.MAX_DEPTH; i++) {
            type = ListMetaDataType.from(type);
        }
        return type;
    }
    private static List<ComplexObject> sampleComplexList() {
        return List.of(
                new ComplexObject(5, new SimpleObject(7)),
                new ComplexObject(6, new SimpleObject(8))
        );
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
