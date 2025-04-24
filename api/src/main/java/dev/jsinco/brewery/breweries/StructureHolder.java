package dev.jsinco.brewery.breweries;

import dev.jsinco.brewery.structure.MultiblockStructure;
import dev.jsinco.brewery.structure.StructureType;
import dev.jsinco.brewery.vector.BreweryLocation;

public interface StructureHolder<H extends StructureHolder<H>> {

    MultiblockStructure<H> getStructure();

    void destroy(BreweryLocation breweryLocation);

    StructureType getStructureType();
}
