package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.api.breweries.InventoryAccessible;
import dev.jsinco.brewery.api.structure.SinglePositionStructure;
import dev.jsinco.brewery.api.structure.StructureType;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BreweryRegistry {

    private final Map<BreweryLocation, SinglePositionStructure> activeSingleBlockStructures = new ConcurrentHashMap<>();
    private final Map<StructureType, Set<InventoryAccessible<ItemStack, Inventory>>> opened = new ConcurrentHashMap<>();
    private final Map<Inventory, InventoryAccessible<ItemStack, Inventory>> inventories = new ConcurrentHashMap<>();

    public Optional<SinglePositionStructure> getActiveSinglePositionStructure(BreweryLocation position) {
        return Optional.ofNullable(activeSingleBlockStructures.get(position));
    }

    public void addActiveSinglePositionStructure(SinglePositionStructure cauldron) {
        activeSingleBlockStructures.put(cauldron.position(), cauldron);
    }

    public void removeActiveSinglePositionStructure(SinglePositionStructure cauldron) {
        activeSingleBlockStructures.remove(cauldron.position());
    }

    public Collection<SinglePositionStructure> getActiveSinglePositionStructure() {
        return activeSingleBlockStructures.values();
    }

    public <H extends InventoryAccessible<ItemStack, Inventory>> Collection<H> getOpened(StructureType structureType) {
        return (Collection<H>) opened.computeIfAbsent(structureType, ignored -> new HashSet<>());
    }

    public <H extends InventoryAccessible<ItemStack, Inventory>> void registerOpened(H holder) {
        StructureType structureType = getStructureType(holder);
        opened.computeIfAbsent(structureType, ignored -> new HashSet<>()).add(holder);
    }

    public <H extends InventoryAccessible<ItemStack, Inventory>> void unregisterOpened(H holder) {
        StructureType structureType = getStructureType(holder);
        opened.computeIfAbsent(structureType, ignored -> new HashSet<>()).remove(holder);
    }

    private <H> StructureType getStructureType(H holder) {
        for (StructureType structureType : dev.jsinco.brewery.api.util.BreweryRegistry.STRUCTURE_TYPE.values()) {
            if (structureType.tClass().isInstance(holder)) {
                return structureType;
            }
        }
        throw new IllegalArgumentException("Holder does not have a matching structure type!");
    }

    public @Nullable InventoryAccessible<ItemStack, Inventory> getFromInventory(Inventory inventory) {
        return inventories.get(inventory);
    }

    public void registerInventory(InventoryAccessible<ItemStack, Inventory> inventoryAccessible) {
        inventoryAccessible.getInventories().forEach(inventory -> inventories.put(inventory, inventoryAccessible));
    }

    public void unregisterInventory(InventoryAccessible<ItemStack, Inventory> inventoryAccessible) {
        inventoryAccessible.getInventories().forEach(inventories::remove);
    }

    public void clear() {
        activeSingleBlockStructures.clear();
        opened.clear();
        inventories.clear();
    }
}
