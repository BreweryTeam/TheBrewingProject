package dev.jsinco.brewery.bukkit.listener;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.InventoryAccessible;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.bukkit.api.event.BarrelInsertEvent;
import dev.jsinco.brewery.bukkit.api.event.BrewModifiableEvent;
import dev.jsinco.brewery.bukkit.api.event.ItemModifiableEvent;
import dev.jsinco.brewery.bukkit.api.event.PermissibleBreweryEvent;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistillery;
import dev.jsinco.brewery.bukkit.effect.named.PukeNamedExecutable;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.database.sql.Database;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class InventoryEventListener implements Listener {

    private final BreweryRegistry registry;
    private final Database database;
    private static final Set<InventoryAction> CLICKED_INVENTORY_ITEM_MOVE = Set.of(InventoryAction.PLACE_SOME,
            InventoryAction.PLACE_ONE, InventoryAction.PLACE_ALL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_ONE);
    private static final Set<InventoryAction> CURSOR_PICKUP = Set.of(
            InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_SOME
    );

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
        // getHotbarButton also returns -1 for offhand clicks
        ItemStack hotbarItem = event.getHotbarButton() == -1 ?
                (event.getClick() == ClickType.SWAP_OFFHAND
                        ? event.getWhoClicked().getInventory().getItemInOffHand()
                        : null)
                : view.getBottomInventory().getItem(event.getHotbarButton());

        ItemStack hoveredItem = event.getCurrentItem();
        Stream<ItemStack> relatedItems;
        ItemStack insertedItem = null;
        ItemStack extractedItem = null;
        if (upperInventoryIsClicked && hoveredItem != null) {
            BrewAdapter.fromItem(hoveredItem)
                    .map(brew -> BrewAdapter.toItem(brew, new BrewImpl.State.Other()))
                    .ifPresent(event::setCurrentItem);
        }
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            // player takes something out
            if (upperInventoryIsClicked && hotbarItem == null) {
                if (triggerExtractEvent(inventoryAccessible, event.getWhoClicked(), false, hoveredItem)) {
                    event.setResult(Event.Result.DENY);
                }
                return;
            }
            extractedItem = hoveredItem;
            insertedItem = hotbarItem;
            relatedItems = Stream.of(hotbarItem, hoveredItem);
        } else if (action == InventoryAction.HOTBAR_SWAP) {
            // barrel not involved
            if (!upperInventoryIsClicked) {
                return;
            }
            extractedItem = hoveredItem;
            insertedItem = hotbarItem;
            relatedItems = Stream.of(hotbarItem, hoveredItem);
        } else {
            ItemStack cursor = event.getCursor();
            relatedItems = Stream.of(cursor);
            if (CURSOR_PICKUP.contains(action)) {
                extractedItem = cursor;
            } else {
                insertedItem = cursor;
            }
        }
        Stream<ItemStack> itemsToCheck = relatedItems
                .filter(Objects::nonNull)
                .filter(item -> !item.getType().isAir());
        boolean cancelled = itemsToCheck.anyMatch(item -> !inventoryAccessible.inventoryAllows(event.getWhoClicked().getUniqueId(), item));
        ItemModifiableEvent extractEvent = extractedItem != null ? triggerExtractEvent(inventoryAccessible, event.getWhoClicked(), cancelled, extractedItem) : null;
        BrewModifiableEvent insertEvent = insertedItem != null ? triggerInsertEvent(inventoryAccessible, event.getWhoClicked(), cancelled, insertedItem) : null;
        if ((extractEvent != null && extractEvent.isCancelled()) || (insertEvent != null && insertEvent.isCancelled())) {
            event.setResult(Event.Result.DENY);
            return;
        }
        if (extractEvent != null) {

        }
        if (insertEvent != null) {

        }
    }

    private BrewModifiableEvent triggerInsertEvent(InventoryAccessible<ItemStack, Inventory> inventoryAccessible, HumanEntity whoClicked, boolean cancelled, ItemStack insertedItem) {
        Player player = whoClicked instanceof Player temp ? temp : null;
        PermissibleBreweryEvent event;
        if (inventoryAccessible instanceof BukkitBarrel barrel) {
            event = new BarrelInsertEvent(barrel, brew);
        }
        if (inventoryAccessible instanceof BukkitDistillery distillery) {

        }
    }

    /**
     *
     * @param inventoryAccessible
     * @param whoClicked
     * @param relatedItems
     * @return True if event result is cancelled
     */
    private ItemModifiableEvent triggerExtractEvent(InventoryAccessible<ItemStack, Inventory> inventoryAccessible, HumanEntity whoClicked, boolean cancelled, ItemStack... relatedItems) {
        Player player = whoClicked instanceof Player temp ? temp : null;
        if (inventoryAccessible instanceof BukkitBarrel barrel) {

        }
        if (inventoryAccessible instanceof BukkitDistillery distillery) {

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

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Optional<InventoryAccessible<ItemStack, Inventory>> source = Optional.ofNullable(registry.getFromInventory(event.getSource()));
        Optional<InventoryAccessible<ItemStack, Inventory>> destination = Optional.ofNullable(registry.getFromInventory(event.getDestination()));
        Optional<InventoryAccessible<ItemStack, Inventory>> both = destination.or(() -> source);
        if (!Config.config().automation()) {
            both.ifPresent(ignored -> event.setCancelled(true));
            return;
        }
        both.filter(inventoryAccessible -> !inventoryAccessible.inventoryAllows(event.getItem()))
                .ifPresent(ignored -> event.setCancelled(true));
        source.flatMap(ignored -> BrewAdapter.fromItem(event.getItem())
                        .map(brew -> BrewAdapter.toItem(brew, new Brew.State.Other())))
                .ifPresent(event::setItem);

    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (event.getItem().getPersistentDataContainer().has(PukeNamedExecutable.PUKE_ITEM)) {
            event.setCancelled(true);
        }
    }
}
