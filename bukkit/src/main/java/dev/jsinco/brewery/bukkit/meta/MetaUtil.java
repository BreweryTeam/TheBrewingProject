package dev.jsinco.brewery.bukkit.meta;

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
            PersistentDataType.LIST.bytes(),
            PersistentDataType.LIST.shorts(),
            PersistentDataType.LIST.integers(),
            PersistentDataType.LIST.longs(),
            PersistentDataType.LIST.floats(),
            PersistentDataType.LIST.doubles(),
            PersistentDataType.LIST.byteArrays(),
            PersistentDataType.LIST.integerArrays(),
            PersistentDataType.LIST.longArrays(),
            PersistentDataType.LIST.dataContainers()
    );

    /* internal */ static PersistentDataType<?, ?> findType(PersistentDataContainer pdc, NamespacedKey key) {
        for (PersistentDataType<?, ?> type : PRIMITIVES) {
            if (pdc.has(key, type)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No type found for " + key);
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
            case List<?> ignored -> UntypedListDataType.INSTANCE;
            default -> throw new IllegalArgumentException("No type found for " + value.getClass().getSimpleName());
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
            case List<?> ignored -> UntypedListDataType.INSTANCE;
            default -> throw new IllegalArgumentException("No type found for " + value.getClass().getSimpleName());
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
