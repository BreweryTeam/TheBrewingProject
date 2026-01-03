package dev.jsinco.brewery.api.breweries;

import dev.jsinco.brewery.api.brew.Brew;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface BrewInventory {

    /**
     *
     * @return All brews in this inventory
     */
    Brew[] getBrews();

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
