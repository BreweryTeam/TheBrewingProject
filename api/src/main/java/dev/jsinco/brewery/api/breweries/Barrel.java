package dev.jsinco.brewery.api.breweries;

import dev.jsinco.brewery.api.structure.StructureType;

/**
 * Due to using too much generics complexity, replaced with {@link BarrelAccess}
 * @param <B> Self
 * @param <IS> ItemStack
 * @param <I> Inventory
 */
@Deprecated(forRemoval = true)
public interface Barrel<B extends Barrel<B, IS, I>, IS, I> extends StructureHolder<B>, InventoryAccessible<IS, I> {

    @Override
    default StructureType getStructureType() {
        return StructureType.BARREL;
    }
}
