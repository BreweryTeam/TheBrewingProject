package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.api.breweries.InventoryAccessible;
import dev.jsinco.brewery.api.structure.SinglePositionStructure;
import dev.jsinco.brewery.api.structure.StructureType;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public final class BreweryRegistry {

    private final Map<BreweryLocation, SinglePositionStructure> activeSingleBlockStructures = new HashMap<>();
    private final Map<StructureType, Set<InventoryAccessible<ItemStack, Inventory>>> opened = new HashMap<>();
    private final Map<Inventory, InventoryAccessible<ItemStack, Inventory>> inventories = new HashMap<>();

    public Optional<SinglePositionStructure> getActiveSinglePositionStructure(BreweryLocation position) {
        return Optional.ofNullable(activeSingleBlockStructures.get(position));
    }

    public synchronized void addActiveSinglePositionStructure(SinglePositionStructure cauldron) {
        activeSingleBlockStructures.put(cauldron.position(), cauldron);
    }

    public synchronized void removeActiveSinglePositionStructure(SinglePositionStructure cauldron) {
        activeSingleBlockStructures.remove(cauldron.position());
    }

    public Collection<SinglePositionStructure> getActiveSinglePositionStructure() {
        return activeSingleBlockStructures.values();
    }
    
    public synchronized <H extends InventoryAccessible<ItemStack, Inventory>> void registerOpened(H holder) {
        StructureType structureType = getStructureType(holder);
        opened.computeIfAbsent(structureType, ignored -> new HashSet<>()).add(holder);
    }

    public synchronized <H extends InventoryAccessible<ItemStack, Inventory>> void unregisterOpened(H holder) {
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

    public synchronized void registerInventory(InventoryAccessible<ItemStack, Inventory> inventoryAccessible) {
        inventoryAccessible.getInventories().forEach(inventory -> inventories.put(inventory, inventoryAccessible));
    }

    public synchronized void unregisterInventory(InventoryAccessible<ItemStack, Inventory> inventoryAccessible) {
        inventoryAccessible.getInventories().forEach(inventories::remove);
    }

    public synchronized void clear() {
        activeSingleBlockStructures.forEach((ignored, structure) -> structure.destroy());
        activeSingleBlockStructures.clear();
        opened.clear();
        inventories.clear();
    }

    public synchronized void iterate(StructureType type, Consumer<InventoryAccessible<ItemStack, Inventory>> inventoryAccessibleAction) {
        Set<InventoryAccessible<ItemStack, Inventory>> inventoryAccessible = opened.get(type);
        if (inventoryAccessible == null) {
            return;
        }
        inventoryAccessible.forEach(inventoryAccessibleAction);
    }
}
