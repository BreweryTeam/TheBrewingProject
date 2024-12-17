package dev.jsinco.brewery.api.world;

import org.bukkit.World;

/**
 * Represents a manager for handling world data on a server.
 */
public interface IWorldDataManager {
    IWorldDataStorage getWorldDataStorage(World world);
}
