package dev.jsinco.brewery.structure;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StructureRegistry {

    private final Map<String, BreweryStructure> structureNames = new HashMap<>();
    private final Map<Material, Set<BreweryStructure>> structures = new HashMap<>();

    public Optional<BreweryStructure> getStructure(@NotNull String key) {
        Preconditions.checkNotNull(key);
        return Optional.ofNullable(structureNames.get(key));
    }

    public Set<BreweryStructure> getPossibleStructures(@NotNull Material material) {
        Preconditions.checkNotNull(material);
        return structures.getOrDefault(material, new HashSet<>());
    }

    public void addStructure(@NotNull String key, @NotNull BreweryStructure structure) {
        Preconditions.checkNotNull(structure);
        Preconditions.checkNotNull(key);
        structureNames.put(key, structure);
        for (BlockData blockData : structure.getPalette()) {
            structures.computeIfAbsent(blockData.getMaterial(), ignored -> new HashSet<>()).add(structure);
        }
    }


    public void addStructures(Map<String, BreweryStructure> stringBreweryStructureMap) {
        for (Map.Entry<String, BreweryStructure> entry : stringBreweryStructureMap.entrySet()) {
            addStructure(entry.getKey(), entry.getValue());
        }
    }
}
