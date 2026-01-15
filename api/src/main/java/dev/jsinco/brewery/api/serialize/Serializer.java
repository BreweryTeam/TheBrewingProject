package dev.jsinco.brewery.api.serialize;

import com.google.common.base.Preconditions;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Serializer<T> {

    /**
     *
     * @param serialized Value in serialized form
     * @return Deserialized value
     */
    T deserialize(String serialized);

    /**
     *
     * @param deserialized Deserialized value
     * @return Value in serialized form
     */
    String serializeSafely(T deserialized);

    /**
     *
     * @param object Object to try serialization on
     * @return Serialized form of object
     * @throws IllegalArgumentException If object is invalid use for this meta
     */
    default String serialize(Object object) throws IllegalArgumentException{
        Preconditions.checkArgument(appliesTo(object), "Invalid deserialization object: " + object);
        return serializeSafely((T) object);
    }

    /**
     *
     * @param object Object to check
     * @return True if object can be serialized and used without any issues
     */
    boolean appliesTo(Object object);

    /**
     * Create a serializer from functions
     * @param serialize Convert value to serialized form
     * @param deserialize Convert value to deserialized form
     * @param appliesTo Check if value is valid
     * @return A new serializer
     * @param <T> The type of the deserialized form
     */
    static <T> Serializer<T> compile(Function<T, String> serialize, Function<String, T> deserialize, Predicate<Object> appliesTo) {
        return new Serializer<>() {
            @Override
            public T deserialize(String serialized) {
                return deserialize.apply(serialized);
            }

            @Override
            public String serializeSafely(T deserialized) {
                return serialize.apply(deserialized);
            }

            @Override
            public boolean appliesTo(Object object) {
                return appliesTo.test(object);
            }
        };
    }

    /**
     * Create a new serializer from a serializer
     * @param toComplex Convert value from previous serializer to new serializer
     * @param toSimple Convert value from new serializer to previous serializer
     * @param appliesTo Check if value is valid
     * @param parent Parent serializer
     * @return A new serializer
     * @param <U> The type of the deserialized form (complex)
     * @param <G> The type of the previous deserialized form (simple)
     */
    static <U, G> Serializer<U> fork(Function<U, G> toComplex, Function<G, U> toSimple, Predicate<Object> appliesTo, Serializer<G> parent) {
        return new Serializer<>() {
            @Override
            public U deserialize(String serialized) {
                return toSimple.apply(parent.deserialize(serialized));
            }

            @Override
            public String serializeSafely(U deserialized) {
                return parent.serializeSafely(toComplex.apply(deserialized));
            }

            @Override
            public boolean appliesTo(Object object) {
                return appliesTo.test(object);
            }
        };
    }

    /**
     * Some more actions need to be done when serializing a string, which is why this class exists.
     */
    class StringMetaSerializer implements Serializer<String> {

        @Override
        public String deserialize(String serialized) {
            // No need to do opposite operation of serialize, as this is already handled by the parser
            return serialized;
        }

        @Override
        public String serializeSafely(String deserialized) {
            return "\"" + deserialized.replace("\\", "\\\\")
                    .replace("\"", "\\\"") + "\"";
        }

        @Override
        public boolean appliesTo(Object object) {
            return object instanceof String;
        }
    }
}
