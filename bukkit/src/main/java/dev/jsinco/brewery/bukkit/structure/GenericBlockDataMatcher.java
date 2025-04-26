package dev.jsinco.brewery.bukkit.structure;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.jetbrains.annotations.Nullable;

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
    public @Nullable Material findSubstitution(BlockData expected, Void ignored) {
        return expected.getMaterial();
    }
}
