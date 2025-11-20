package dev.jsinco.brewery.api.structure;

import com.google.gson.JsonElement;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.api.util.HolderProviderHolder;
import dev.jsinco.brewery.api.vector.BreweryVector;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
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
    public static final StructureMeta<MaterialTag> MIXTURE_MATERIAL_TAG = new StructureMeta<>(
            BreweryKey.parse("mixture_material_tag"),
            MaterialTag.class,
            new MaterialTag(Set.of(Holder.Material.fromMinecraftId("decorated_pot")), 1, 2, 1)
    );
    public static final StructureMeta<MaterialTag> DISTILLATE_MATERIAL_TAG = new StructureMeta<>(
            BreweryKey.parse("distillate_material_tag"),
            MaterialTag.class,
            new MaterialTag(Set.of(Holder.Material.fromMinecraftId("decorated_pot")), 1, 1, 1)
    );
    public static final StructureMeta<BreweryVector.List> DISTILLATE_ACCESS_POINTS = new StructureMeta<>(
            BreweryKey.parse("distillate_access_points"),
            BreweryVector.List.class,
            null
    );
    public static final StructureMeta<BreweryVector.List> MIXTURE_ACCESS_POINTS = new StructureMeta<>(
            BreweryKey.parse("mixture_access_points"),
            BreweryVector.List.class,
            null
    );
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


}
