package dev.jsinco.brewery.breweries;

import dev.jsinco.brewery.structure.StructureType;

public interface Barrel<B extends Barrel<B, IS, I>, IS, I> extends StructureHolder<B>, InventoryAccessible<IS, I> {

    @Override
    default StructureType getStructureType() {
        return StructureType.BARREL;
    }
}
