package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BrewInventory implements InventoryHolder {

    private final Inventory inventory;
    @Getter
    private final Brew[] brews;
    private final BrewPersistenceHandler store;

    public BrewInventory(String title, int size, BrewPersistenceHandler store) {
        this.inventory = Bukkit.createInventory(this, size, title);
        this.brews = new Brew[size];
        this.store = store;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Set an item in the inventory without changing the database
     *
     * @param brew
     * @param position
     */
    public void set(@Nullable Brew brew, int position) {
        brews[position] = brew;
    }

    public void updateInventoryFromBrews() {
        for (int i = 0; i < brews.length; i++) {
            Brew brew = brews[i];
            if (brew == null) {
                inventory.setItem(i, null);
                continue;
            }
            inventory.setItem(i, BrewAdapter.toItem(brew, new Brew.State.Brewing()));
        }
    }

    public boolean updateBrewsFromInventory() {
        boolean hasUpdated = false;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            Brew brew;
            if (itemStack == null) {
                brew = null;
            } else {
                brew = BrewAdapter.fromItem(itemStack).orElse(null);
            }
            if (!Objects.equals(brew, brews[i])) {
                hasUpdated = true;
            }
            store(brew, i);
        }
        return hasUpdated;
    }

    public void store(Brew brew, int position) {
        this.store.store(brew, position, this);
        set(brew, position);
    }

    public boolean isEmpty() {
        for (Brew brew : brews) {
            if (brew != null) {
                return false;
            }
        }
        return true;
    }

    public boolean isFull() {
        for (Brew brew : brews) {
            if (brew == null) {
                return false;
            }
        }
        return true;
    }

    public int brewAmount() {
        int amount = 0;
        for (Brew brew : brews) {
            if (brew != null) {
                amount++;
            }
        }
        return amount;
    }

    public List<ItemStack> destroy() {
        List<ItemStack> output = new ArrayList<>();
        List.copyOf(inventory.getViewers()).forEach(HumanEntity::closeInventory);
        this.inventory.clear();
        for (Brew brew : brews) {
            if (brew == null) {
                continue;
            }
            output.add(BrewAdapter.toItem(brew, new Brew.State.Other()));
        }
        return output;
    }
}
