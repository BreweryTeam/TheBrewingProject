package dev.jsinco.brewery.structure;

import dev.jsinco.brewery.breweries.StructureHolder;
import dev.jsinco.brewery.vector.BreweryLocation;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PlacedStructureRegistry {

    void registerStructure(MultiblockStructure<?> placedBreweryStructure);

    void unregisterStructure(MultiblockStructure<?> structure);

    Optional<MultiblockStructure<?>> getStructure(BreweryLocation location);

    Set<MultiblockStructure<?>> getStructures(Collection<BreweryLocation> locations);

    Optional<StructureHolder<?>> getHolder(BreweryLocation location);

    void unloadWorld(UUID worldUuid);

    void clear();
}
