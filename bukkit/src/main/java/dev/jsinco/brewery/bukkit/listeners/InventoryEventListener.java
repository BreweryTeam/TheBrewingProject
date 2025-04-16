package dev.jsinco.brewery.bukkit.listeners;

import dev.jsinco.brewery.brew.Brew;
import dev.jsinco.brewery.breweries.InventoryAccessible;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.database.sql.Database;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class InventoryEventListener implements Listener {

    private final BreweryRegistry registry;
    private final Database database;
    private static final Set<InventoryAction> CLICKED_INVENTORY_ITEM_MOVE = Set.of(InventoryAction.PLACE_SOME,
            InventoryAction.PLACE_ONE, InventoryAction.PLACE_ALL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_ONE);
    private static final Set<InventoryAction> TRANSFER_HOVERED_ITEM = Set.of(InventoryAction.MOVE_TO_OTHER_INVENTORY,
            InventoryAction.HOTBAR_SWAP);

    public InventoryEventListener(BreweryRegistry registry, Database database) {
        this.registry = registry;
        this.database = database;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryAccessible<ItemStack, Inventory> inventoryAccessible = registry.getFromInventory(event.getInventory());
        if (inventoryAccessible == null) {
            return;
        }
        InventoryAction action = event.getAction();
        if (action == InventoryAction.NOTHING) {
            return;
        }
        boolean upperInventoryIsClicked = event.getClickedInventory() == event.getInventory();
        if (!upperInventoryIsClicked && CLICKED_INVENTORY_ITEM_MOVE.contains(action)) {
            return;
        }
        InventoryView view = event.getView();
        ItemStack hotbarItem = event.getHotbarButton() == -1 ? null : view.getBottomInventory().getItem(event.getHotbarButton());
        ItemStack hoveredItem = event.getCurrentItem();
        Stream<ItemStack> relatedItems;
        if (upperInventoryIsClicked) {
            int slot = event.getRawSlot();
            ItemStack initial = view.getItem(slot);
            if (initial != null) {
                view.setItem(slot, BrewAdapter.fromItem(initial)
                        .map(brew -> BrewAdapter.toItem(brew, Brew.State.OTHER))
                        .orElse(initial));
            }
        }
        if (TRANSFER_HOVERED_ITEM.contains(action)) {
            if (upperInventoryIsClicked && hotbarItem == null) {
                return;
            }
            relatedItems = Stream.of(hotbarItem, hoveredItem);
        } else {
            ItemStack cursor = event.getCursor();
            relatedItems = Stream.of(cursor);
        }
        Stream<ItemStack> itemsToCheck = relatedItems
                .filter(Objects::nonNull)
                .filter(item -> !item.getType().isAir());
        if (itemsToCheck.anyMatch(item -> !inventoryAccessible.inventoryAllows(event.getWhoClicked().getUniqueId(), item))) {
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent dragEvent) {
        InventoryAccessible<ItemStack, Inventory> inventoryAccessible = registry.getFromInventory(dragEvent.getInventory());
        if (inventoryAccessible == null) {
            return;
        }
        InventoryView inventoryView = dragEvent.getView();
        if (!dragEvent.getNewItems().entrySet().stream()
                .filter(entry -> dragEvent.getInventory() == inventoryView.getInventory(entry.getKey()))
                .map(Map.Entry::getValue)
                .allMatch(itemStack -> inventoryAccessible.inventoryAllows(dragEvent.getWhoClicked().getUniqueId(), itemStack))) {
            dragEvent.setResult(Event.Result.DENY);
        }
    }
}
