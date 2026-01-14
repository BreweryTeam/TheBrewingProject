package dev.jsinco.brewery.bukkit.meta;

import dev.jsinco.brewery.api.meta.ListMetaDataType;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Common type utilities for MetaData <-> PDC conversion.
 */
/* internal */ final class MetaUtil {
    private MetaUtil() {}

    private static final List<PersistentDataType<?, ?>> PRIMITIVES = List.of(
            PersistentDataType.BYTE,
            PersistentDataType.SHORT,
            PersistentDataType.INTEGER,
            PersistentDataType.LONG,
            PersistentDataType.FLOAT,
            PersistentDataType.DOUBLE,
            PersistentDataType.STRING,
            PersistentDataType.BYTE_ARRAY,
            PersistentDataType.INTEGER_ARRAY,
            PersistentDataType.LONG_ARRAY,
            PersistentDataType.TAG_CONTAINER,
            UntypedListDataType.INSTANCE
    );

    /* internal */ static PersistentDataType<?, ?> findType(PersistentDataContainer pdc, NamespacedKey key) {
        return findType(pdc, key, PRIMITIVES, 0);
    }
    private static PersistentDataType<?, ?> findType(PersistentDataContainer pdc, NamespacedKey key,
                                                     List<? extends PersistentDataType<?, ?>> types, int depth) {
        if (depth > ListMetaDataType.MAX_DEPTH) {
            throw new IllegalArgumentException("No type found for " + key);
        }
        for (PersistentDataType<?, ?> type : types) {
            if (pdc.has(key, type)) {
                return type;
            }
        }
        return findType(pdc, key, types.stream().map(PersistentDataType.LIST::listTypeFrom).toList(), depth + 1);
    }

    /* internal */ static PersistentDataType<?, ?> pdcTypeOf(Object value) {
        return switch (value) {
            case Byte ignored -> PersistentDataType.BYTE;
            case Short ignored -> PersistentDataType.SHORT;
            case Integer ignored -> PersistentDataType.INTEGER;
            case Long ignored -> PersistentDataType.LONG;
            case Float ignored -> PersistentDataType.FLOAT;
            case Double ignored -> PersistentDataType.DOUBLE;
            case String ignored -> PersistentDataType.STRING;
            case byte[] ignored -> PersistentDataType.BYTE_ARRAY;
            case int[] ignored -> PersistentDataType.INTEGER_ARRAY;
            case long[] ignored -> PersistentDataType.LONG_ARRAY;
            case MetaData ignored -> MetaDataPdcType.INSTANCE;
            case PersistentDataContainer ignored -> PersistentDataType.TAG_CONTAINER;
            case List<?> list -> pdcListTypeOf(list, 1);
            default -> throw new IllegalArgumentException("No type found for " + value.getClass().getSimpleName());
        };
    }

    private static PersistentDataType<?, ?> pdcListTypeOf(List<?> list, int depth) {
        if (depth > ListMetaDataType.MAX_DEPTH) {
            throw new IllegalArgumentException("Requested pdc type of list that was too deeply nested");
        }
        if (list.isEmpty()) {
            return PersistentDataType.LIST.bytes();
        }
        Object element = list.getFirst();
        return switch (element) {
            case Byte ignored -> PersistentDataType.LIST.bytes();
            case Short ignored -> PersistentDataType.LIST.shorts();
            case Integer ignored -> PersistentDataType.LIST.integers();
            case Long ignored -> PersistentDataType.LIST.longs();
            case Float ignored -> PersistentDataType.LIST.floats();
            case Double ignored -> PersistentDataType.LIST.doubles();
            case String ignored -> PersistentDataType.LIST.strings();
            case byte[] ignored -> PersistentDataType.LIST.byteArrays();
            case int[] ignored -> PersistentDataType.LIST.integerArrays();
            case long[] ignored -> PersistentDataType.LIST.longArrays();
            case MetaData ignored -> MetaDataPdcType.LIST;
            case PersistentDataContainer ignored -> PersistentDataType.LIST.dataContainers();
            case List<?> nestedList -> PersistentDataType.LIST.listTypeFrom(pdcListTypeOf(nestedList, depth + 1));
            default -> throw new IllegalArgumentException("No type found for list element " + element.getClass().getSimpleName());
        };
    }

    /* internal */ static MetaDataType<?, ?> metaDataTypeOf(Object value) {
        return switch (value) {
            case Byte ignored -> MetaDataType.BYTE;
            case Short ignored -> MetaDataType.SHORT;
            case Integer ignored -> MetaDataType.INTEGER;
            case Long ignored -> MetaDataType.LONG;
            case Float ignored -> MetaDataType.FLOAT;
            case Double ignored -> MetaDataType.DOUBLE;
            case String ignored -> MetaDataType.STRING;
            case byte[] ignored -> MetaDataType.BYTE_ARRAY;
            case int[] ignored -> MetaDataType.INTEGER_ARRAY;
            case long[] ignored -> MetaDataType.LONG_ARRAY;
            case MetaData ignored -> MetaDataType.CONTAINER;
            case PersistentDataContainer pdc -> PdcMetaDataType.with(pdc.getAdapterContext());
            case List<?> list -> metaDataListTypeOf(list, 1);
            default -> throw new IllegalArgumentException("No type found for " + value.getClass().getSimpleName());
        };
    }

    private static MetaDataType<?, ?> metaDataListTypeOf(List<?> list, int depth) {
        if (depth > ListMetaDataType.MAX_DEPTH) {
            throw new IllegalArgumentException("Requested meta data type of list that was too deeply nested");
        }
        if (list.isEmpty()) {
            return MetaDataType.BYTE_LIST;
        }
        Object element = list.getFirst();
        return switch (element) {
            case Byte ignored -> MetaDataType.BYTE_LIST;
            case Short ignored -> MetaDataType.SHORT_LIST;
            case Integer ignored -> MetaDataType.INTEGER_LIST;
            case Long ignored -> MetaDataType.LONG_LIST;
            case Float ignored -> MetaDataType.FLOAT_LIST;
            case Double ignored -> MetaDataType.DOUBLE_LIST;
            case String ignored -> MetaDataType.STRING_LIST;
            case byte[] ignored -> MetaDataType.BYTE_ARRAY_LIST;
            case int[] ignored -> MetaDataType.INTEGER_ARRAY_LIST;
            case long[] ignored -> MetaDataType.LONG_ARRAY_LIST;
            case MetaData ignored -> MetaDataType.CONTAINER_LIST;
            case PersistentDataContainer pdc -> ListMetaDataType.from(PdcMetaDataType.with(pdc.getAdapterContext()));
            case List<?> nestedList -> ListMetaDataType.from(metaDataListTypeOf(nestedList, depth + 1));
            default -> throw new IllegalArgumentException("No type found for list element " + element.getClass().getSimpleName());
        };
    }

    private static class UntypedListDataType implements MetaDataType<List<?>, List<?>>, PersistentDataType<List<?>, List<?>> {

        public static final UntypedListDataType INSTANCE = new UntypedListDataType();

        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public Class<List<?>> getPrimitiveType() {
            return (Class<List<?>>) (Object) List.class;
        }

        @NotNull
        @Override
        @SuppressWarnings("unchecked")
        public Class<List<?>> getComplexType() {
            return (Class<List<?>>) (Object) List.class;
        }

        @NotNull
        @Override
        public List<?> toPrimitive(@NotNull List<?> complex, @NotNull PersistentDataAdapterContext context) {
            return complex;
        }
        @Override
        public List<?> toPrimitive(List<?> complex) {
            return complex;
        }

        @NotNull
        @Override
        public List<?> fromPrimitive(@NotNull List<?> primitive, @NotNull PersistentDataAdapterContext context) {
            return primitive;
        }
        @Override
        public List<?> toComplex(List<?> primitive) {
            return primitive;
        }

    }

}
