package dev.jsinco.brewery.breweries;

import dev.jsinco.brewery.structure.MultiblockStructure;
import dev.jsinco.brewery.structure.StructureType;
import dev.jsinco.brewery.vector.BreweryLocation;

public interface StructureHolder<H extends StructureHolder<H>> {

    /**
     * @return The structure this is linked to
     */
    MultiblockStructure<H> getStructure();

    /**
     * Persistently destroy the structure
     *
     * @param breweryLocation The location to destroy from
     */
    void destroy(BreweryLocation breweryLocation);

    /**
     * @return The type of the structure
     */
    StructureType getStructureType();
}
