package dev.jsinco.brewery.api.serialize;

import com.google.common.base.Preconditions;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Serializer<T> {

    T deserialize(String serialized);

    String serialize(T deserialized);

    default String serializeSafely(Object object) {
        Preconditions.checkArgument(appliesTo(object), "Invalid deserialization object: " + object);
        return serialize((T) object);
    }

    boolean appliesTo(Object obj);

    static <T> Serializer<T> compile(Function<T, String> serialize, Function<String, T> deserialize, Predicate<Object> appliesTo) {
        return new Serializer<>() {
            @Override
            public T deserialize(String serialized) {
                return deserialize.apply(serialized);
            }

            @Override
            public String serialize(T deserialized) {
                return serialize.apply(deserialized);
            }

            @Override
            public boolean appliesTo(Object obj) {
                return appliesTo.test(obj);
            }
        };
    }

    static <U, G> Serializer<U> fork(Function<U, G> serialize, Function<G, U> deserialize, Predicate<Object> appliesTo, Serializer<G> parent) {
        return new Serializer<>() {
            @Override
            public U deserialize(String serialized) {
                return deserialize.apply(parent.deserialize(serialized));
            }

            @Override
            public String serialize(U deserialized) {
                return parent.serialize(serialize.apply(deserialized));
            }

            @Override
            public boolean appliesTo(Object obj) {
                return appliesTo.test(obj);
            }
        };
    }

    class StringSerializer implements Serializer<String> {

        @Override
        public String deserialize(String serialized) {
            return serialized;
        }

        @Override
        public String serialize(String deserialized) {
            return "\"" + deserialized.replace("\\", "\\\\")
                    .replace("\"", "\\\"") + "\"";
        }

        @Override
        public boolean appliesTo(Object obj) {
            return false;
        }
    }
}
