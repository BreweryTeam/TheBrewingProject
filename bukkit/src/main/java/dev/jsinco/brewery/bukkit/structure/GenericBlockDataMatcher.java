package dev.jsinco.brewery.bukkit.structure;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;

import java.util.Set;
import java.util.stream.Collectors;

public class GenericBlockDataMatcher implements BlockDataMatcher<Void> {

    public static final GenericBlockDataMatcher INSTANCE = new GenericBlockDataMatcher();

    @Override
    public boolean matches(BlockData actual, BlockData expected, Void ignored) {
        // Avoid being too strict, think of fences for example
        if (expected instanceof MultipleFacing) {
            return actual.getMaterial().equals(expected.getMaterial());
        }
        if (expected.getMaterial() == Material.DECORATED_POT) {
            return actual.getMaterial() == Material.DECORATED_POT;
        }
        return actual.equals(expected);
    }

    @Override
    public Set<Material> findStructureMaterials(Void matcherType, BreweryStructure structure) {
        return structure.getPalette().stream()
                .map(BlockData::getMaterial)
                .collect(Collectors.toSet());
    }
}
