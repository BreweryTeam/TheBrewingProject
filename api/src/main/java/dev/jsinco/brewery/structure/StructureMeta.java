package dev.jsinco.brewery.structure;

import com.google.gson.JsonElement;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.BreweryKeyed;
import dev.jsinco.brewery.util.BreweryRegistry;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @param key          The key of the meta
 * @param validator    The validator of the metadata
 * @param deserializer A deserializer for the metadata
 * @param defaultValue The default value for this meta
 * @param <V>          The metadata type
 */
public record StructureMeta<V>(BreweryKey key, Predicate<Object> validator, Function<JsonElement, V> deserializer,
                               V defaultValue) implements BreweryKeyed {

    public static final StructureMeta<Boolean> USE_BARREL_SUBSTITUTION = new StructureMeta<>(BreweryKey.parse("use_barrel_substitution"), Boolean.class::isInstance, JsonElement::getAsBoolean, false);
    public static final StructureMeta<Integer> INVENTORY_SIZE = new StructureMeta<>(BreweryKey.parse("inventory_size"), value ->
            value instanceof Integer integer && integer % 9 == 0 && integer > 0,
            JsonElement::getAsInt,
            9);
    public static final StructureMeta<String> TAGGED_MATERIAL = new StructureMeta<>(BreweryKey.parse("tagged_material"), String.class::isInstance, JsonElement::getAsString, "decorated_pot");
    public static final StructureMeta<Long> PROCESS_TIME = new StructureMeta<>(BreweryKey.parse("process_time"), Long.class::isInstance, JsonElement::getAsLong, 80L);
    public static final StructureMeta<Integer> PROCESS_AMOUNT = new StructureMeta<>(BreweryKey.parse("process_amount"), Integer.class::isInstance, JsonElement::getAsInt, 1);
    // Keep this at the bottom, going to cause issues because of class initialization order otherwise
    public static final StructureMeta<StructureType> TYPE = new StructureMeta<>(BreweryKey.parse("type"), StructureType.class::isInstance, jsonElement -> BreweryRegistry.STRUCTURE_TYPE.get(BreweryKey.parse(jsonElement.getAsString().toLowerCase(Locale.ROOT))), StructureType.BARREL);

    @Override
    public String toString() {
        return "StructureMeta(" + key + ")";
    }
}
