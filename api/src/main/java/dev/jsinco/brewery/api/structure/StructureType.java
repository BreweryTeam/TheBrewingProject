package dev.jsinco.brewery.api.structure;

import dev.jsinco.brewery.api.breweries.Barrel;
import dev.jsinco.brewery.api.breweries.BarrelAccess;
import dev.jsinco.brewery.api.breweries.Distillery;
import dev.jsinco.brewery.api.breweries.DistilleryAccess;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @param key           The key of the structure type
 * @param tClass        The class of the structure type
 * @param mandatoryMeta The mandatory structure meta for this structure type
 */
public record StructureType<T>(BreweryKey key, Class<T> tClass,
                               StructureMeta<?>... mandatoryMeta) implements BreweryKeyed {

    public static final StructureType<BarrelAccess> BARREL = new StructureType<>(BreweryKey.parse("barrel"), BarrelAccess.class, StructureMeta.INVENTORY_SIZE);
    public static final StructureType<DistilleryAccess> DISTILLERY = new StructureType<>(BreweryKey.parse("distillery"), DistilleryAccess.class, StructureMeta.INVENTORY_SIZE, StructureMeta.PROCESS_TIME, StructureMeta.PROCESS_AMOUNT);

    /**
     * @param actualMeta The meta to check for
     * @return The missing structure meta for this structure type
     */
    public List<StructureMeta<?>> getMissingMandatory(Collection<StructureMeta<?>> actualMeta) {
        return Arrays.stream(mandatoryMeta).filter(value -> !actualMeta.contains(value)).toList();
    }
}
