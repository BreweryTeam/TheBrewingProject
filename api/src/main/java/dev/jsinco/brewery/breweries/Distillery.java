package dev.jsinco.brewery.breweries;

import dev.jsinco.brewery.structure.StructureType;

public interface Distillery<D extends Distillery<D, IS, I>, IS, I> extends StructureHolder<D>, InventoryAccessible<IS, I> {

    long getStartTime();

    void tick();

    @Override
    default StructureType getStructureType() {
        return StructureType.DISTILLERY;
    }
}
