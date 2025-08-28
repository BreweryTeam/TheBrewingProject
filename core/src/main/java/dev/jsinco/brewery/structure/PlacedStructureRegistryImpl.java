package dev.jsinco.brewery.structure;

import dev.jsinco.brewery.api.breweries.StructureHolder;
import dev.jsinco.brewery.api.structure.MultiblockStructure;
import dev.jsinco.brewery.api.structure.PlacedStructureRegistry;
import dev.jsinco.brewery.api.structure.StructureType;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import dev.jsinco.brewery.api.vector.BreweryVector;

import java.util.*;

public class PlacedStructureRegistryImpl implements PlacedStructureRegistry {

    private final Map<UUID, Map<BreweryVector, MultiblockStructure<? extends StructureHolder<?>>>> structures = new HashMap<>();
    private final Map<StructureType, Set<MultiblockStructure<?>>> typedMultiBlockStructureMap = new HashMap<>();

    public void registerStructure(MultiblockStructure<?> multiblockStructure) {
        for (BreweryLocation location : multiblockStructure.positions()) {
            UUID worldUuid = location.worldUuid();
            structures.computeIfAbsent(worldUuid, ignored -> new HashMap<>()).put(location.toVector(), multiblockStructure);
        }
        typedMultiBlockStructureMap.computeIfAbsent(multiblockStructure.getHolder().getStructureType(), ignored -> new HashSet<>()).add(multiblockStructure);
    }

    public void unregisterStructure(MultiblockStructure<?> structure) {
        for (BreweryLocation location : structure.positions()) {
            UUID worldUuid = location.worldUuid();
            structures.computeIfAbsent(worldUuid, ignored -> new HashMap<>()).remove(location.toVector());
        }
        typedMultiBlockStructureMap.computeIfAbsent(structure.getHolder().getStructureType(), ignored -> new HashSet<>()).remove(structure);
    }

    public Optional<MultiblockStructure<?>> getStructure(BreweryLocation location) {
        UUID worldUuid = location.worldUuid();
        Map<BreweryVector, MultiblockStructure<?>> placedBreweryStructureMap = structures.getOrDefault(worldUuid, new HashMap<>());
        return Optional.ofNullable(placedBreweryStructureMap.get(location.toVector()));
    }

    public Set<MultiblockStructure<?>> getStructures(Collection<BreweryLocation> locations) {
        Set<MultiblockStructure<?>> breweryStructures = new HashSet<>();
        for (BreweryLocation location : locations) {
            getStructure(location).ifPresent(breweryStructures::add);
        }
        return breweryStructures;
    }

    public Set<MultiblockStructure<?>> getStructures(StructureType structureType) {
        return typedMultiBlockStructureMap.computeIfAbsent(structureType, ignored -> new HashSet<>());
    }

    public Optional<StructureHolder<?>> getHolder(BreweryLocation location) {
        UUID worldUuid = location.worldUuid();
        Map<BreweryVector, MultiblockStructure<? extends StructureHolder<?>>> placedBreweryStructureMap = structures.getOrDefault(worldUuid, new HashMap<>());
        return Optional.ofNullable(placedBreweryStructureMap.get(location.toVector()))
                .map(MultiblockStructure::getHolder);
    }

    public void unloadWorld(UUID worldUuid) {
        Map<BreweryVector, MultiblockStructure<? extends StructureHolder<?>>> removed = structures.remove(worldUuid);
        if (removed == null) {
            return;
        }
        removed.forEach((ignored1, structure) -> {
            typedMultiBlockStructureMap.computeIfAbsent(structure.getHolder().getStructureType(), ignored2 -> new HashSet<>()).remove(structure);
        });
    }

    public void clear() {
        structures.clear();
    }
}
