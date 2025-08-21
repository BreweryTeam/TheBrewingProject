package dev.jsinco.brewery.bukkit.integration;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface StructureIntegration extends Integration {
    boolean hasAccess(Block block, Player player);
}
