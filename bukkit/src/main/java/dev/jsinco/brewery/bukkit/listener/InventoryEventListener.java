package dev.jsinco.brewery.bukkit.listener;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.InventoryAccessible;
import dev.jsinco.brewery.bukkit.api.event.ItemTransactionEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransaction;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.bukkit.effect.named.PukeNamedExecutable;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.database.sql.Database;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class InventoryEventListener implements Listener {

    private final BreweryRegistry registry;
    private final Database database;
    private static final Set<InventoryAction> CLICKED_INVENTORY_ITEM_MOVE = Set.of(InventoryAction.PLACE_SOME,
            InventoryAction.PLACE_ONE, InventoryAction.PLACE_ALL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_ONE, InventoryAction.SWAP_WITH_CURSOR);
    private static final Set<InventoryAction> CURSOR = Set.of(
            InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ALL,
            InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ALL
    );
    private static final Set<InventoryAction> ITEM_PICKUP_CURSOR = Set.of(
            InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ALL
    );
    private static final Set<InventoryAction> BANNED = Set.of(
            InventoryAction.PICKUP_FROM_BUNDLE, InventoryAction.PLACE_FROM_BUNDLE, InventoryAction.PICKUP_ALL_INTO_BUNDLE,
            InventoryAction.PLACE_ALL_INTO_BUNDLE, InventoryAction.PLACE_SOME_INTO_BUNDLE, InventoryAction.PICKUP_SOME_INTO_BUNDLE
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
        if (BANNED.contains(action)) {
            event.setCancelled(true);
            return;
        }
        boolean upperInventoryIsClicked = event.getClickedInventory() == event.getInventory();
        if (!upperInventoryIsClicked && CLICKED_INVENTORY_ITEM_MOVE.contains(action)) {
            return;
        }
        List<ItemTransactionEvent<?>> transactions = compileTransactionsFromClick(event, upperInventoryIsClicked, inventoryAccessible);

    }

    private List<ItemTransactionEvent<?>> compileTransactionsFromClick(InventoryClickEvent event, boolean upperInventoryIsClicked,
                                                                       InventoryAccessible<ItemStack, Inventory> inventoryAccessible) {
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (upperInventoryIsClicked) {
                return eventsFromStructure(
                        inventoryAccessible,
                        new ItemTransaction.RawPosition(event.getRawSlot()),
                        findEmptyPositions(event.getView(), event.getCurrentItem(), false),
                        event.getCurrentItem(),
                        true
                );
            } else {
                return eventsFromStructure(
                        inventoryAccessible,
                        new ItemTransaction.RawPosition(event.getRawSlot()),
                        findEmptyPositions(event.getView(), event.getCurrentItem(), true),
                        event.getCurrentItem(),
                        false
                );
            }
        }
        if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            if (upperInventoryIsClicked) {
                return List.of();
            }
            int hotbar = event.getHotbarButton() == -1 ?
                    (event.getClick() == ClickType.SWAP_OFFHAND ? 40 : -1)
                    : event.getHotbarButton();
            if (hotbar == -1) {
                return List.of();
            }
            Stream.Builder<ItemTransactionEvent<?>> eventBuilder = Stream.builder();
            eventsFromStructure(
                    inventoryAccessible,
                    new ItemTransaction.RawPosition(hotbar),
                    List.of(new ItemTransaction.RawPosition(event.getRawSlot())),
                    event.getCurrentItem(),
                    true
            ).forEach(eventBuilder::add);
            eventsFromStructure(
                    inventoryAccessible,
                    new ItemTransaction.RawPosition(event.getRawSlot()),
                    List.of(new ItemTransaction.RawPosition(hotbar)),
                    event.getView().getItem(hotbar),
                    false
            ).forEach(eventBuilder::add);
            return eventBuilder.build().toList();
        }
        if (CURSOR.contains(event.getAction())) {
            if (InventoryAction.SWAP_WITH_CURSOR == event.getAction()) {
                Stream.Builder<ItemTransactionEvent<?>> eventBuilder = Stream.builder();
                eventsFromStructure(
                        inventoryAccessible,
                        new ItemTransaction.Cursor(),
                        List.of(new ItemTransaction.RawPosition(event.getRawSlot())),
                        event.getCursor(),
                        true
                ).forEach(eventBuilder::add);
                eventsFromStructure(
                        inventoryAccessible,
                        new ItemTransaction.RawPosition(event.getRawSlot()),
                        List.of(new ItemTransaction.Cursor()),
                        event.getCurrentItem(),
                        false
                ).forEach(eventBuilder::add);
                return eventBuilder.build().toList();
            }
            if (ITEM_PICKUP_CURSOR.contains(event.getAction())) {
                return eventsFromStructure(
                        inventoryAccessible,
                        new ItemTransaction.RawPosition(event.getRawSlot()),
                        List.of(new ItemTransaction.Cursor()),
                        event.getCurrentItem(),
                        false
                );
            }
            return eventsFromStructure(
                    inventoryAccessible,
                    new ItemTransaction.Cursor(),
                    List.of(new ItemTransaction.RawPosition(event.getRawSlot())),
                    event.getCursor(),
                    true
            );
        }
        return List.of();
    }

    private List<ItemTransaction.InventoryPosition> findEmptyPositions(InventoryView view, @Nullable ItemStack currentItem, boolean topInventory) {
        if (currentItem == null) {
            return List.of();
        }
        Inventory inventory = topInventory ? view.getTopInventory() : view.getBottomInventory();
        int amount = currentItem.getAmount();
        List<ItemTransaction.InventoryPosition> positions = new ArrayList<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.isEmpty()) {
                positions.add(new ItemTransaction.RawPosition(i));
                return positions;
            }
            if (!item.isSimilar(currentItem)) {
                continue;
            }
            positions.add(new ItemTransaction.RawPosition(i));
            amount -= item.getMaxStackSize() - item.getAmount();
            if (amount <= 0) {
                break;
            }
        }
        return positions;
    }

    private static List<ItemTransactionEvent<?>> eventsFromStructure(InventoryAccessible<ItemStack, Inventory> inventoryAccessible,
                                                                     ItemTransaction.InventoryPosition from, List<ItemTransaction.InventoryPosition> to,
                                                                     ItemStack item, boolean insertion) {

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