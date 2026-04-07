package dev.jsinco.brewery.bukkit.structure;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.structure.StructureMeta;
import dev.jsinco.brewery.api.structure.StructureType;
import org.bukkit.block.BlockType;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class StructureRegistry {

    private final Map<String, BreweryStructure> structureNames = new HashMap<>();
    private final Map<StructureType, Map<BlockType, Set<BreweryStructure>>> structuresWithMaterials = new HashMap<>();
    private final Map<StructureType, Set<BreweryStructure>> structures = new HashMap<>();

    public Optional<BreweryStructure> getStructure(@NonNull String key) {
        Preconditions.checkNotNull(key);
        return Optional.ofNullable(structureNames.get(key));
    }

    public Set<BreweryStructure> getPossibleStructures(@NonNull BlockType material, StructureType structureType) {
        Preconditions.checkNotNull(material);
        return structuresWithMaterials.computeIfAbsent(structureType, ignored -> new HashMap<>()).getOrDefault(material, Set.of());
    }

    public void addStructure(@NonNull BreweryStructure structure) {
        Preconditions.checkNotNull(structure);
        structureNames.put(structure.getName(), structure);
        structures.computeIfAbsent(structure.getMeta(StructureMeta.TYPE), ignored -> new HashSet<>()).add(structure);
        for (StructureMatcher structureMatcher : structure.getStructureMatchers()) {
            Set<BlockType> possibleMaterials = structureMatcher.dumpBlockTypes();
            Map<BlockType, Set<BreweryStructure>> materialStructureMap = structuresWithMaterials.computeIfAbsent(structure.getMeta(StructureMeta.TYPE), ignored -> new HashMap<>());
            possibleMaterials.forEach(material -> materialStructureMap.computeIfAbsent(material, ignored -> new HashSet<>()).add(structure));
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
