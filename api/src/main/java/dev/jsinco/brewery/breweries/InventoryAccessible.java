package dev.jsinco.brewery.breweries;

import dev.jsinco.brewery.vector.BreweryLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @param <IS> Item stack type
 * @param <I>  Inventory type
 */
public interface InventoryAccessible<IS, I> {

    /**
     * @param breweryLocation The location to access an inventory from
     * @param playerUuid      The uuid of the player accessing the inventory
     * @return True if an inventory opened
     */
    boolean open(@NotNull BreweryLocation breweryLocation, @NotNull UUID playerUuid);

    /**
     * @param silent Don't play any sounds if silent
     */
    void close(boolean silent);

    /**
     * @param playerUuid The uuid of the player accessing the inventory
     * @param item       An item stack
     * @return True if the item stack and player is allowed to inside this inventory (IS Usually gets wiped otherwise)
     */
    boolean inventoryAllows(@NotNull UUID playerUuid, @NotNull IS item);

    /**
     * @param item An item stack
     * @return True if the item stack is allowed inside the inventory (IS Usually gets wiped otherwise)
     */
    boolean inventoryAllows(@NotNull IS item);

    /**
     * @return A set of all inventories for this
     */
    Set<I> getInventories();

    /**
     * Update this inventory for players to view
     */
    void tickInventory();

    /**
     * @param breweryLocation The location to access the inventory from
     * @return An optionally present inventory if any were linked to the location
     */
    Optional<I> access(@NotNull BreweryLocation breweryLocation);
}
