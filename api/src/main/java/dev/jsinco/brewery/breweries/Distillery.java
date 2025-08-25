package dev.jsinco.brewery.breweries;

import dev.jsinco.brewery.structure.StructureType;

public interface Distillery<D extends Distillery<D, IS, I>, IS, I> extends StructureHolder<D>, InventoryAccessible<IS, I>, Tickable {

    /**
     * @return The Time when brewing started (internal plugin time)
     */
    long getStartTime();

    @Override
    default StructureType getStructureType() {
        return StructureType.DISTILLERY;
    }
}
