package dev.jsinco.brewery.api.structure;

import com.google.gson.JsonElement;
import dev.jsinco.brewery.api.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @param key          The key of the meta
 * @param validator    The validator of the metadata
 * @param deserializer A deserializer for the metadata
 * @param defaultValue The default value for this meta
 * @param <V>          The metadata type
 */
public record StructureMeta<V>(BreweryKey key, Predicate<Object> validator,
                               Function<JsonElement, V> deserializer,
                               V defaultValue) implements BreweryKeyed {

    public static final StructureMeta<Boolean> USE_BARREL_SUBSTITUTION = new StructureMeta<>(
            BreweryKey.parse("use_barrel_substitution"),
            Boolean.class::isInstance,
            JsonElement::getAsBoolean,
            false);
    public static final StructureMeta<Integer> INVENTORY_SIZE = new StructureMeta<>(BreweryKey.parse("inventory_size"), value ->
            value instanceof Integer integer && integer % 9 == 0 && integer > 0,
            JsonElement::getAsInt,
            9);
    public static final StructureMeta<String> TAGGED_MATERIAL = new StructureMeta<>(
            BreweryKey.parse("tagged_material"),
            String.class::isInstance,
            JsonElement::getAsString,
            "decorated_pot");
    public static final StructureMeta<Long> PROCESS_TIME = new StructureMeta<>(
            BreweryKey.parse("process_time"),
            Long.class::isInstance,
            JsonElement::getAsLong,
            80L);
    public static final StructureMeta<Integer> PROCESS_AMOUNT = new StructureMeta<>(
            BreweryKey.parse("process_amount"),
            Integer.class::isInstance,
            JsonElement::getAsInt,
            1);
    public static final StructureMeta<List<BlockMatcherReplacement>> BLOCK_REPLACEMENTS = new StructureMeta<>(BreweryKey.parse("replacements"), List.class::isInstance,
            jsonElement -> jsonElement.getAsJsonArray().asList().stream()
                    .map(StructureMeta::deserializeReplacement)
                    .toList(),
            List.of()
    );

    // Keep this at the bottom, going to cause issues because of class initialization order otherwise
    public static final StructureMeta<StructureType> TYPE = new StructureMeta<>(BreweryKey.parse("type"), StructureType.class::isInstance, jsonElement -> BreweryRegistry.STRUCTURE_TYPE.get(BreweryKey.parse(jsonElement.getAsString())), StructureType.BARREL);

    @Override
    public @NotNull String toString() {
        return "StructureMeta(" + key + ")";
    }

    private static BlockMatcherReplacement deserializeReplacement(JsonElement element) {
        JsonElement original = element.getAsJsonObject().get("original");
        Holder.Material originalHolder = HolderProviderHolder.instance().material(original.getAsString())
                .orElseThrow(() -> new IllegalArgumentException("Expected a valid material, got: " + original.getAsString()));
        JsonElement replacement = element.getAsJsonObject().get("replacement");
        if (replacement.isJsonArray()) {
            return new BlockMatcherReplacement(
                    replacement.getAsJsonArray().asList().stream()
                            .map(JsonElement::getAsString)
                            .flatMap(StructureMeta::parseMaterials)
                            .collect(Collectors.toUnmodifiableSet()),
                    originalHolder
            );
        }
        return new BlockMatcherReplacement(parseMaterials(replacement.getAsString()).collect(Collectors.toUnmodifiableSet()), originalHolder);
    }

    private static Stream<Holder.Material> parseMaterials(String string) {
        if (string.startsWith("#")) {
            return HolderProviderHolder.instance().parseTag(string.replaceFirst("#", "")).stream();
        }
        return HolderProviderHolder.instance().material(string).stream();
    }
}
