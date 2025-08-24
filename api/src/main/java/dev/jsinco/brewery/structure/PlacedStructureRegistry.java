package dev.jsinco.brewery.structure;

import dev.jsinco.brewery.breweries.StructureHolder;
import dev.jsinco.brewery.vector.BreweryLocation;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * ALL OPERATIONS DONE ON THIS REGISTRY ARE <b>NOT</b> GOING TO BE PERSISTENT
 */
public interface PlacedStructureRegistry {

    /**
     * @param placedBreweryStructure The multi block structure to register
     */
    void registerStructure(MultiblockStructure<?> placedBreweryStructure);

    /**
     * @param structure The multiblock structure to unregister
     */
    void unregisterStructure(MultiblockStructure<?> structure);

    /**
     * @param location The location to check for a structure
     * @return An optionally present structure if matches
     */
    Optional<MultiblockStructure<?>> getStructure(BreweryLocation location);

    /**
     * @param locations The locations to check for structures
     * @return A set of all matching structures
     */
    Set<MultiblockStructure<?>> getStructures(Collection<BreweryLocation> locations);

    /**
     * Utility method
     *
     * @param location The location to check for a structure
     * @return An optionally present structure holder if matches
     */
    default Optional<StructureHolder<?>> getHolder(BreweryLocation location) {
        return getStructure(location).map(MultiblockStructure::getHolder);
    }

    /**
     * @param worldUuid The world to unload all structures in
     */
    void unloadWorld(UUID worldUuid);

    /**
     * Clear this registry
     */
    void clear();
}
