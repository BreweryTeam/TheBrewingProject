package dev.jsinco.brewery.bukkit.structure;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.Set;

public interface BlockDataMatcher<T> {

    boolean matches(BlockData actual, BlockData expected, T matcherType);

    Set<Material> findStructureMaterials(T matcherType, BreweryStructure structure);
}
