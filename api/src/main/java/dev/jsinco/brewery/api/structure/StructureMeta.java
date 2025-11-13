package dev.jsinco.brewery.api.structure;

import com.google.gson.JsonElement;
import dev.jsinco.brewery.api.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @param key          The key of the meta
 * @param vClass       The class
 * @param defaultValue The default value for this meta
 * @param <V>          The metadata type
 */
public record StructureMeta<V>(BreweryKey key, Class<V> vClass, V defaultValue) implements BreweryKeyed {

    public static final StructureMeta<Boolean> USE_BARREL_SUBSTITUTION = new StructureMeta<>(
            BreweryKey.parse("use_barrel_substitution"),
            Boolean.class,
            false);
    public static final StructureMeta<Integer> INVENTORY_SIZE = new StructureMeta<>(BreweryKey.parse("inventory_size"),
            Integer.class,
            9);
    public static final StructureMeta<String> TAGGED_MATERIAL = new StructureMeta<>(
            BreweryKey.parse("tagged_material"),
            String.class,
            "decorated_pot");
    public static final StructureMeta<Long> PROCESS_TIME = new StructureMeta<>(
            BreweryKey.parse("process_time"),
            Long.class,
            80L);
    public static final StructureMeta<Integer> PROCESS_AMOUNT = new StructureMeta<>(
            BreweryKey.parse("process_amount"),
            Integer.class,
            1);
    public static final StructureMeta<BlockMatcherReplacement.List> BLOCK_REPLACEMENTS = new StructureMeta<>(
            BreweryKey.parse("replacements"),
            BlockMatcherReplacement.List.class,
            new BlockMatcherReplacement.List()
    );

    // Keep this at the bottom, going to cause issues because of class initialization order otherwise
    public static final StructureMeta<StructureType> TYPE = new StructureMeta<>(BreweryKey.parse("type"), StructureType.class, StructureType.BARREL);

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
