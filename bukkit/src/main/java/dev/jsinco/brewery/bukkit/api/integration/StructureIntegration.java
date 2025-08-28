package dev.jsinco.brewery.bukkit.api.integration;

import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.util.BreweryKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface StructureIntegration extends Integration {

    /**
     * Whether a given player has access to a structure at the given location
     */
    boolean hasAccess(Block block, Player player, BreweryKey type);
}
