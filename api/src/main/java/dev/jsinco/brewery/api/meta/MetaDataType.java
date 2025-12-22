package dev.jsinco.brewery.api.meta;

import net.kyori.adventure.key.Key;

/**
 * Represents a type that a metadata value can have.
 * <p>
 *     All static variables of this interface are <strong>primitive</strong> metadata types.
 *     Primitive types are stored when the metadata is serialized.
 * </p>
 * <p>
 *     Users can implement this interface to store custom, more complex types.
 *     See {@link BooleanMetaDataType} for an example.
 * </p>
 * @param <P> The primitive Java type that is stored when serialized,
 *           <strong>must absolutely be one of the primitive types listed in this interface!</strong>
 * @param <C> The Java type that is retrieved when using {@link MetaContainer#meta(Key, MetaDataType)}
 */
public interface MetaDataType<P, C> {

    MetaDataType<Byte, Byte> BYTE = new Primitive<>(Byte.class);
    MetaDataType<Short, Short> SHORT = new Primitive<>(Short.class);
    MetaDataType<Integer, Integer> INTEGER = new Primitive<>(Integer.class);
    MetaDataType<Long, Long> LONG = new Primitive<>(Long.class);
    MetaDataType<Float, Float> FLOAT = new Primitive<>(Float.class);
    MetaDataType<Double, Double> DOUBLE = new Primitive<>(Double.class);
    MetaDataType<String, String> STRING = new Primitive<>(String.class);

    MetaDataType<byte[], byte[]> BYTE_ARRAY = new Primitive<>(byte[].class);
    MetaDataType<int[], int[]> INTEGER_ARRAY = new Primitive<>(int[].class);
    MetaDataType<long[], long[]> LONG_ARRAY = new Primitive<>(long[].class);

    /**
     * The metadata type for nested metadata.
     */
    MetaDataType<MetaData, MetaData> CONTAINER = new Primitive<>(MetaData.class);

    ListMetaDataType<Byte, Byte> BYTE_LIST = ListMetaDataType.from(MetaDataType.BYTE);
    ListMetaDataType<Short, Short> SHORT_LIST = ListMetaDataType.from(MetaDataType.SHORT);
    ListMetaDataType<Integer, Integer> INTEGER_LIST = ListMetaDataType.from(MetaDataType.INTEGER);
    ListMetaDataType<Long, Long> LONG_LIST = ListMetaDataType.from(MetaDataType.LONG);
    ListMetaDataType<Float, Float> FLOAT_LIST = ListMetaDataType.from(MetaDataType.FLOAT);
    ListMetaDataType<Double, Double> DOUBLE_LIST = ListMetaDataType.from(MetaDataType.DOUBLE);
    ListMetaDataType<String, String> STRING_LIST = ListMetaDataType.from(MetaDataType.STRING);

    ListMetaDataType<byte[], byte[]> BYTE_ARRAY_LIST = ListMetaDataType.from(MetaDataType.BYTE_ARRAY);
    ListMetaDataType<int[], int[]> INTEGER_ARRAY_LIST = ListMetaDataType.from(MetaDataType.INTEGER_ARRAY);
    ListMetaDataType<long[], long[]> LONG_ARRAY_LIST = ListMetaDataType.from(MetaDataType.LONG_ARRAY);

    ListMetaDataType<MetaData, MetaData> CONTAINER_LIST = ListMetaDataType.from(MetaDataType.CONTAINER);

    /**
     * @return The class of the primitive Java type
     */
    Class<P> getPrimitiveType();

    /**
     * @return The class of the complex Java type
     */
    Class<C> getComplexType();

    /**
     * Reduces a complex object to this type's primitive, which can later be passed to {@link #toComplex(Object)}
     * to reconstruct the original object.
     * @param complex The complex object
     * @return The primitive value
     */
    P toPrimitive(C complex);

    /**
     * Reconstructs a complex object from its primitive form created from {@link #toPrimitive(Object)}.
     * This method may assume the primitive is valid and throw exceptions if this assumption does not hold.
     * @param primitive The primitive value
     * @return The complex object
     */
    C toComplex(P primitive);

    class Primitive<P> implements MetaDataType<P, P> {
        private final Class<P> primitiveClass;

        private Primitive(Class<P> primitiveClass) {
            this.primitiveClass = primitiveClass;
        }

        @Override
        public Class<P> getPrimitiveType() {
            return primitiveClass;
        }
        @Override
        public Class<P> getComplexType() {
            return primitiveClass;
        }

        @Override
        public P toPrimitive(P complex) {
            return complex;
        }
        @Override
        public P toComplex(P primitive) {
            return primitive;
        }
    }

}
