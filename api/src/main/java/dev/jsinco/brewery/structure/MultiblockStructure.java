package dev.jsinco.brewery.structure;

import dev.jsinco.brewery.breweries.StructureHolder;
import dev.jsinco.brewery.vector.BreweryLocation;

import java.util.List;

public interface MultiblockStructure<H extends StructureHolder<H>> {

    /**
     * @return The block positions of this structure
     */
    List<BreweryLocation> positions();

    /**
     * @return A behavior holder
     */
    H getHolder();

    /**
     * @param holder A behavior holder
     */
    void setHolder(H holder);

    /**
     * @return A unique position to identify this structure
     */
    BreweryLocation getUnique();
}
