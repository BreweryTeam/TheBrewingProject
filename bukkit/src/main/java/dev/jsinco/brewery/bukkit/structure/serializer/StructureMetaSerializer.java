package dev.jsinco.brewery.bukkit.structure.serializer;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.structure.MaterialTag;
import dev.jsinco.brewery.api.structure.StructureMeta;
import dev.jsinco.brewery.api.structure.StructureType;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.bukkit.structure.BreweryStructure;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StructureMetaSerializer implements ObjectSerializer<BreweryStructure.Meta> {


    @Override
    public boolean supports(@NonNull Class<? super BreweryStructure.Meta> type) {
        return BreweryStructure.Meta.class == type;
    }

    @Override
    public void serialize(BreweryStructure.@NonNull Meta object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        for (Map.Entry<StructureMeta<?>, Object> entry : object.data().entrySet()) {
            data.add(entry.getKey().key().minimalized(), entry.getValue());
        }
    }

    @Override
    public BreweryStructure.Meta deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        Set<String> keys = data.asMap().keySet();
        Map<StructureMeta<?>, Object> meta = new HashMap<>();
        for (String key : keys) {
            BreweryKey breweryKey = BreweryKey.parse(key);
            // Backwards compatibility handling
            if (breweryKey.equals(BreweryKey.parse("tagged_material"))) {
                Set<Holder.Material> materials = data.getAsSet(key, Holder.Material.class);
                meta.put(StructureMeta.DISTILLATE_MATERIAL_TAG, new MaterialTag(
                        materials,
                        1,
                        1,
                        1)
                );
                meta.put(StructureMeta.MIXTURE_MATERIAL_TAG, new MaterialTag(
                        materials,
                        1,
                        2,
                        1)
                );
                continue;
            }
            StructureMeta<?> metaItem = BreweryRegistry.STRUCTURE_META.get(breweryKey);
            Preconditions.checkArgument(metaItem != null, "Unknown meta: " + key);
            meta.put(metaItem, data.get(key, metaItem.vClass()));
        }
        Preconditions.checkArgument(meta.containsKey(StructureMeta.TYPE), "Expected structure type to be present");
        StructureType type = (StructureType) meta.get(StructureMeta.TYPE);
        List<StructureMeta<?>> missing = type.getMissingMandatory(meta.keySet());
        Preconditions.checkArgument(missing.isEmpty(), "Structure is missing the following meta: " + missing);
        if (type == StructureType.DISTILLERY) {
            Preconditions.checkArgument(meta.containsKey(StructureMeta.MIXTURE_MATERIAL_TAG) || meta.containsKey(StructureMeta.MIXTURE_ACCESS_POINTS), "Missing meta 'mixture_material_tag' or 'mixture_access_points'!");
            Preconditions.checkArgument(meta.containsKey(StructureMeta.DISTILLATE_MATERIAL_TAG) || meta.containsKey(StructureMeta.DISTILLATE_ACCESS_POINTS), "Missing meta 'distillate_material_tag' or 'distillate_access_points'");
        }
        return new BreweryStructure.Meta(meta);
    }
}
