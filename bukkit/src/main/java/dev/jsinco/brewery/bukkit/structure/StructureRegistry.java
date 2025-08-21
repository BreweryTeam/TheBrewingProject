package dev.jsinco.brewery.bukkit.structure;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.structure.StructureMeta;
import dev.jsinco.brewery.structure.StructureType;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StructureRegistry {

    private final Map<String, BreweryStructure> structureNames = new HashMap<>();
    private final Map<StructureType, Map<Material, Set<BreweryStructure>>> structuresWithMaterials = new HashMap<>();
    private final Map<StructureType, Set<BreweryStructure>> structures = new HashMap<>();

    public Optional<BreweryStructure> getStructure(@NotNull String key) {
        Preconditions.checkNotNull(key);
        return Optional.ofNullable(structureNames.get(key));
    }

    public Set<BreweryStructure> getPossibleStructures(@NotNull Material material, StructureType structureType) {
        Preconditions.checkNotNull(material);
        return structuresWithMaterials.computeIfAbsent(structureType, ignored -> new HashMap<>()).getOrDefault(material, Set.of());
    }

    public <T> void addStructure(@NotNull BreweryStructure structure, BlockDataMatcher<T> blockDataMatcher, T[] matcherTypes) {
        Preconditions.checkNotNull(structure);
        structureNames.put(structure.getName(), structure);
        structures.computeIfAbsent(structure.getMeta(StructureMeta.TYPE), ignored -> new HashSet<>()).add(structure);
        for (T matcherType : matcherTypes) {
            Set<Material> possibleMaterials = blockDataMatcher.findStructureMaterials(matcherType, structure);
            Map<Material, Set<BreweryStructure>> materialStructureMap = structuresWithMaterials.computeIfAbsent(structure.getMeta(StructureMeta.TYPE), ignored -> new HashMap<>());
            possibleMaterials.forEach(material -> materialStructureMap.computeIfAbsent(material, ignored -> new HashSet<>()).add(structure));
        }

    }

    public void addStructure(@NotNull BreweryStructure structure) {
        Preconditions.checkNotNull(structure);
        structureNames.put(structure.getName(), structure);
        structures.computeIfAbsent(structure.getMeta(StructureMeta.TYPE), ignored -> new HashSet<>()).add(structure);
        for (BlockData blockData : structure.getPalette()) {
            structuresWithMaterials.computeIfAbsent(structure.getMeta(StructureMeta.TYPE), ignored -> new HashMap<>())
                    .computeIfAbsent(blockData.getMaterial(), ignored -> new HashSet<>()).add(structure);
        }
    }

    public Collection<BreweryStructure> getStructures(StructureType structureType) {
        return structures.computeIfAbsent(structureType, ignored -> new HashSet<>());
    }

    public void clear() {
        structures.clear();
        structureNames.clear();
        structuresWithMaterials.clear();
    }
}
