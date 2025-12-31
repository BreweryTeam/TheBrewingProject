package dev.jsinco.brewery.api.breweries;

import dev.jsinco.brewery.api.structure.MultiblockStructure;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface BarrelAccess {

    /**
     * Open this barrels inventory for the player with the specified UUID
     * @param location The location to open from
     * @param playerUuid The player UUID
     * @return True if canceled
     */
    boolean open(@NotNull BreweryLocation location, @NotNull UUID playerUuid);

    /**
     * Closes the barrel inventory for all viewers
     * @param silent Whether to play a barrel sound or not
     */
    void close(boolean silent);

    /**
     * Destroy the barrel
     * @param breweryLocation The location to destroy from
     */
    void destroy(BreweryLocation breweryLocation);

    /**
     * @return The barrels inventory
     */
    BrewInventory getBrewInventory();

    /**
     * @return The underlying barrel structure
     */
    MultiblockStructure<? extends BarrelAccess> getStructure();
}
