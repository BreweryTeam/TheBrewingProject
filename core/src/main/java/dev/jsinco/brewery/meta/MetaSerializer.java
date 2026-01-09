package dev.jsinco.brewery.meta;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jsinco.brewery.api.meta.ListMetaDataType;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.meta.MetaDataType;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MetaSerializer {

    /*
     * Serializes MetaData to/from the following format:
     *
     * {
     *   "<type><key>": <value>,
     *   "[<key>": [
     *     <type>,        // not present if list is empty
     *     <values...>
     *   ],
     *   "{<key>": <meta>
     * }
     *
     * where <type> is a single character representing the type of the value (see Primitive)
     */

    public static final MetaSerializer INSTANCE = new MetaSerializer();

    private static final Gson GSON = new Gson();
    private static final char LIST_TYPE_CHARACTER = '[';
    private static final char META_TYPE_CHARACTER = '{';

    public JsonObject serialize(MetaData meta) {
        JsonObject json = new JsonObject();
        for (Map.Entry<Key, Object> entry : meta.primitiveMap().entrySet()) {
            Key metaKey = entry.getKey();
            Object value = entry.getValue();
            switch (value) {
                case List<?> list -> json.add(LIST_TYPE_CHARACTER + metaKey.toString(), serializedList(list));
                case MetaData nestedMeta -> json.add(META_TYPE_CHARACTER + metaKey.toString(), serialize(nestedMeta));
                default -> json.add(typeof(value).typeCharacter + metaKey.toString(), GSON.toJsonTree(value));
            }
        }
        return json;
    }

    private JsonArray serializedList(List<?> list) {
        if (list.isEmpty()) {
            return new JsonArray();
        }
        Object first = list.getFirst();
        char typeCharacter = switch (first) {
            case List<?> ignored -> LIST_TYPE_CHARACTER;
            case MetaData ignored -> META_TYPE_CHARACTER;
            default -> typeof(first).typeCharacter;
        };
        JsonArray arr = new JsonArray();
        arr.add(typeCharacter);
        list.stream()
                .map(value -> switch (value) {
                    case List<?> nestedList -> serializedList(nestedList);
                    case MetaData meta -> serialize(meta);
                    default -> GSON.toJsonTree(value);
                })
                .forEach(arr::add);
        return arr;
    }

    public MetaData deserialize(JsonObject json) {
        MetaData meta = new MetaData();
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String jsonKey = entry.getKey();
            JsonElement jsonValue = entry.getValue();
            char typeCharacter = jsonKey.charAt(0);
            Key metaKey = Key.key(jsonKey.substring(1));
            meta = switch (typeCharacter) {
                case LIST_TYPE_CHARACTER -> withList(meta, metaKey, jsonValue.getAsJsonArray());
                case META_TYPE_CHARACTER -> meta.withMeta(metaKey, MetaDataType.CONTAINER, deserialize(jsonValue.getAsJsonObject()));
                default -> primitiveFromChar(typeCharacter).metaWithValue(meta, metaKey, jsonValue);
            };
        }
        return meta;
    }

    @SuppressWarnings("unchecked") // Stream<List<?>>.toList() is a List<List<?>>
    private MetaData withList(MetaData meta, Key metaKey, JsonArray arr) {
        if (arr.isEmpty()) {
            return meta.withMeta(metaKey, MetaDataType.STRING_LIST, List.of());
        }
        char typeCharacter = arr.get(0).getAsString().charAt(0);
        arr.remove(0);
        return switch (typeCharacter) {
            case LIST_TYPE_CHARACTER -> {
                List<List<?>> list = (List<List<?>>) (Object) arr.asList().stream()
                        .map(JsonElement::getAsJsonArray)
                        .map(this::convertTypedArray)
                        .toList();
                yield meta.withMeta(metaKey, UntypedListDataType.INSTANCE, list);
            }
            case META_TYPE_CHARACTER -> {
                List<MetaData> list = arr.asList().stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(this::deserialize)
                        .toList();
                yield meta.withMeta(metaKey, MetaDataType.CONTAINER_LIST, list);
            }
            default -> primitiveFromChar(typeCharacter).metaWithListOfValues(meta, metaKey, arr);
        };
    }

    private List<?> convertTypedArray(JsonArray arr) {
        if (arr.isEmpty()) {
            return List.of();
        }
        char typeCharacter = arr.get(0).getAsString().charAt(0);
        arr.remove(0);
        return switch (typeCharacter) {
            case LIST_TYPE_CHARACTER -> arr.asList().stream()
                    .map(JsonElement::getAsJsonArray)
                    .map(this::convertTypedArray)
                    .toList();
            case META_TYPE_CHARACTER -> arr.asList().stream()
                    .map(JsonElement::getAsJsonObject)
                    .map(this::deserialize)
                    .toList();
            default -> primitiveFromChar(typeCharacter).asList(arr);
        };
    }

    private static final List<Primitive<?>> PRIMITIVES = List.of(
            new Primitive<>('b', MetaDataType.BYTE, MetaDataType.BYTE_LIST, JsonElement::getAsByte),
            new Primitive<>('s', MetaDataType.SHORT, MetaDataType.SHORT_LIST, JsonElement::getAsShort),
            new Primitive<>('i', MetaDataType.INTEGER, MetaDataType.INTEGER_LIST, JsonElement::getAsInt),
            new Primitive<>('l', MetaDataType.LONG, MetaDataType.LONG_LIST, JsonElement::getAsLong),
            new Primitive<>('f', MetaDataType.FLOAT, MetaDataType.FLOAT_LIST, JsonElement::getAsFloat),
            new Primitive<>('d', MetaDataType.DOUBLE, MetaDataType.DOUBLE_LIST, JsonElement::getAsDouble),
            new Primitive<>('S', MetaDataType.STRING, MetaDataType.STRING_LIST, JsonElement::getAsString),
            new Primitive<>('B', MetaDataType.BYTE_ARRAY, MetaDataType.BYTE_ARRAY_LIST, e -> GSON.fromJson(e, byte[].class)),
            new Primitive<>('I', MetaDataType.INTEGER_ARRAY, MetaDataType.INTEGER_ARRAY_LIST, e -> GSON.fromJson(e, int[].class)),
            new Primitive<>('L', MetaDataType.LONG_ARRAY, MetaDataType.LONG_ARRAY_LIST, e -> GSON.fromJson(e, long[].class))
    );

    private static Primitive<?> primitiveFromChar(char typeCharacter) {
        return PRIMITIVES.stream()
                .filter(p -> p.typeCharacter == typeCharacter)
                .findFirst()
                .orElseThrow();
    }

    private static Primitive<?> typeof(Object value) {
        return switch (value) {
            case Byte ignored -> PRIMITIVES.get(0);
            case Short ignored -> PRIMITIVES.get(1);
            case Integer ignored -> PRIMITIVES.get(2);
            case Long ignored -> PRIMITIVES.get(3);
            case Float ignored -> PRIMITIVES.get(4);
            case Double ignored -> PRIMITIVES.get(5);
            case String ignored -> PRIMITIVES.get(6);
            case byte[] ignored -> PRIMITIVES.get(7);
            case int[] ignored -> PRIMITIVES.get(8);
            case long[] ignored -> PRIMITIVES.get(9);
            default -> throw new IllegalArgumentException("No type found for " + value.getClass().getSimpleName());
        };
    }

    private record Primitive<P>(
            char typeCharacter,
            MetaDataType<P, P> type,
            ListMetaDataType<P, P> listType,
            Function<JsonElement, P> jsonToPrimitive
    ) {

        public MetaData metaWithValue(MetaData meta, Key metaKey, JsonElement value) {
            return meta.withMeta(metaKey, type, jsonToPrimitive.apply(value));
        }

        public MetaData metaWithListOfValues(MetaData meta, Key metaKey, JsonArray arr) {
            return meta.withMeta(metaKey, listType, asList(arr));
        }

        public List<P> asList(JsonArray arr) {
            return arr.asList().stream()
                    .map(jsonToPrimitive)
                    .toList();
        }

    }

    // A data type that reads and writes lists as-is, without any type transformation
    private static class UntypedListDataType implements MetaDataType<List<?>, List<?>> {

        public static final UntypedListDataType INSTANCE = new UntypedListDataType();

        @Override
        @SuppressWarnings("unchecked")
        public Class<List<?>> getPrimitiveType() {
            return (Class<List<?>>) (Object) List.class;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<List<?>> getComplexType() {
            return (Class<List<?>>) (Object) List.class;
        }

        @Override
        public List<?> toPrimitive(List<?> complex) {
            return complex;
        }

        @Override
        public List<?> toComplex(List<?> primitive) {
            return primitive;
        }

    }

}
