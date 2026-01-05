package dev.jsinco.brewery.api.serialize;

import com.google.common.base.Preconditions;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Serializer<T> {

    T deserialize(String serialized);

    String serialize(T deserialized);

    default String serializeSafely(Object object){
        Preconditions.checkArgument(appliesTo(object), "Invalid deserialization object: " + object);
        return serialize((T) object);
    }

    boolean appliesTo(Object obj);

    static <T> Serializer<T> compile(Function<T, String> serialize, Function<String, T> deserialize, Predicate<Object> appliesTo) {
        return new Serializer<T>() {
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
}
