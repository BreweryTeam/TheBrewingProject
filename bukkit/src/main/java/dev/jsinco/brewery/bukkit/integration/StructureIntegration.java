package dev.jsinco.brewery.bukkit.integration;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface StructureIntegration extends Integration {

    /**
     * Whether a given player has access to a structure at the given location
     */
    boolean hasAccess(Block block, Player player);
}
