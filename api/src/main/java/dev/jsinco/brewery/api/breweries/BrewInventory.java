package dev.jsinco.brewery.api.breweries;

import dev.jsinco.brewery.api.brew.Brew;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface BrewInventory {

    /**
     * Gets a view of brews in the inventory, where each index of the array corresponds to a position in the inventory.
     * Empty slots are represented by null.
     *
     * @return All brews in this inventory
     */
    Brew[] getBrews();

    /**
     * Gets a snapshot of all brews currently in the inventory. The list does not update as the inventory updates.
     *
     * @return All brews in this inventory
     */
    default List<Brew> getBrewSnapshot() {
        return Arrays.stream(getBrews())
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Set an item in the inventory without changing the database
     *
     * @param brew The brew to set
     * @param position The position of the brew to set
     */
    void set(@Nullable Brew brew, int position);

    /**
     * Update the inventory GUI from brew inventory
     */
    void updateInventoryFromBrews();

    /**
     * Update the brew inventory from inventory GUI
     * @return True if inventory GUI had any changes
     */
    boolean updateBrewsFromInventory();

    /**
     * Store an item in the inventory, also modify the database
     * @param brew The brew to store
     * @param position The position to store the brew
     */
    void store(Brew brew, int position);

    /**
     * @return True if inventory is empty
     */
    boolean isEmpty();

    /**
     * @return True if inventory is full
     */
    boolean isFull();

    /**
     * @return The amount of brews in this inventory
     */
    int brewAmount();
}
