package dev.jsinco.brewery.structure;

import dev.jsinco.brewery.breweries.StructureHolder;
import dev.jsinco.brewery.vector.BreweryLocation;

import java.util.List;

public interface MultiblockStructure<H extends StructureHolder<H>> {

    List<BreweryLocation> positions();

    H getHolder();

    void setHolder(H holder);

    BreweryLocation getUnique();
}
