package dev.jsinco.brewery.api.breweries;

import dev.jsinco.brewery.api.structure.MultiblockStructure;
import dev.jsinco.brewery.api.util.CancelState;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface DistilleryAccess {
    /**
     * Open this distillery inventory for the player with the specified UUID
     * @param location The location to open from
     * @param player The player UUID
     * @return True if canceled
     */
    CancelState open(@NotNull BreweryLocation location, @NotNull Holder.Player player);

    /**
     * Closes the distillery inventory for all viewers
     * @param silent Whether to play a close sound or not
     */
    void close(boolean silent);

    /**
     * Destroy the distillery, dropping all contained items
     * @param breweryLocation The location to destroy from
     */
    void destroy(BreweryLocation breweryLocation);

    /**
     * @return This distillery mixture inventory
     */
    BrewInventory getMixture();

    /**
     * @return This distillery distillate inventory
     */
    BrewInventory getDistillate();

    /**
     * @return The underlying distillery structure
     */
    MultiblockStructure<? extends DistilleryAccess> getStructure();

}
