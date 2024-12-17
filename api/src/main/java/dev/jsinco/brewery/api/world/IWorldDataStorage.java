package dev.jsinco.brewery.api.world;

import dev.jsinco.brewery.api.math.BlockPos;
import dev.jsinco.brewery.api.world.block.AbstractBlock;

/**
 * Manages data in a single world
 */
public interface IWorldDataStorage {
    AbstractBlock getBlock(BlockPos pos);
    AbstractBlock getBlock(int x, int y, int z);
}
