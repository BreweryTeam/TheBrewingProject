package dev.jsinco.brewery.bukkit.listener;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.InventoryAccessible;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.event.*;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransaction;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistillery;
import dev.jsinco.brewery.bukkit.effect.named.PukeNamedExecutable;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.database.sql.Database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InventoryEventListener implements Listener {

    private final BreweryRegistry registry;
    private final Database database;
    private static final Set<InventoryAction> CLICKED_INVENTORY_ITEM_MOVE = Set.of(InventoryAction.PLACE_SOME,
            InventoryAction.PLACE_ONE, InventoryAction.PLACE_ALL, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_ONE, InventoryAction.SWAP_WITH_CURSOR);
    private static final Set<InventoryAction> CURSOR = Set.of(
            InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ALL,
            InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ALL, InventoryAction.SWAP_WITH_CURSOR
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
        List<? extends ItemTransactionEvent<?>> transactions = compileTransactionsFromClick(event, upperInventoryIsClicked, inventoryAccessible);
        for (ItemTransactionEvent<?> transactionEvent : transactions) {
            if (!transactionEvent.callEvent()) {
                if (transactionEvent.isDenied()) {
                    event.getWhoClicked().sendMessage(transactionEvent.getDenyMessage());
                }
                event.setResult(Event.Result.DENY);
                return;
            }
        }
        Bukkit.getScheduler().runTask(TheBrewingProject.getInstance(), () -> {
            for (ItemTransactionEvent<?> transactionEvent : transactions) {
                ItemTransactionSession<?> session = transactionEvent.getTransactionSession();
                ItemTransaction transaction = session.getTransaction();
                ItemStack itemStack = session.getResult() == null ? null : session.getResult().get();
                if (transaction.to() instanceof ItemTransaction.Cursor) {
                    event.getView().setCursor(itemStack);
                }
                if (transaction.to() instanceof ItemTransaction.RawPosition(int pos)) {
                    event.getView().setItem(pos, itemStack);
                }
                if (transaction.to() instanceof ItemTransaction.UpperInventoryPosition(int pos)) {
                    event.getView().getTopInventory().setItem(pos, itemStack);
                }
                if (transaction.to() instanceof ItemTransaction.LowerInventoryPosition(int pos)) {
                    event.getView().getBottomInventory().setItem(pos, itemStack);
                }
            }
        });
    }

    private List<? extends ItemTransactionEvent<?>> compileTransactionsFromClick(InventoryClickEvent event, boolean upperInventoryIsClicked,
                                                                                 InventoryAccessible<ItemStack, Inventory> inventoryAccessible) {
        Player player = event.getWhoClicked() instanceof Player temp ? temp : null;
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (upperInventoryIsClicked) {
                return findEmptyPositions(event.getView(), event.getCurrentItem(), false)
                        .stream()
                        .map(inventoryPosition -> eventFromStructure(
                                inventoryAccessible,
                                new ItemTransaction.RawPosition(event.getRawSlot()),
                                inventoryPosition,
                                event.getCurrentItem(),
                                false,
                                player
                        ))
                        .toList();
            } else {
                return findEmptyPositions(event.getView(), event.getCurrentItem(), true)
                        .stream()
                        .map(inventoryPosition -> eventFromStructure(
                                inventoryAccessible,
                                new ItemTransaction.RawPosition(event.getRawSlot()),
                                inventoryPosition,
                                event.getCurrentItem(),
                                true,
                                player
                        ))
                        .toList();
            }
        }
        if (event.getAction() == InventoryAction.HOTBAR_SWAP) {
            if (!upperInventoryIsClicked) {
                return List.of();
            }
            int hotbar = event.getHotbarButton() == -1 ?
                    (event.getClick() == ClickType.SWAP_OFFHAND ? 40 : -1)
                    : event.getHotbarButton();
            if (hotbar == -1) {
                return List.of();
            }
            ItemStack currentItem = event.getCurrentItem();
            List<ItemTransactionEvent<?>> output = new ArrayList<>();
            if (currentItem != null && !currentItem.isEmpty()) {
                output.add(eventFromStructure(
                        inventoryAccessible,
                        new ItemTransaction.RawPosition(event.getRawSlot()),
                        new ItemTransaction.LowerInventoryPosition(hotbar),
                        currentItem,
                        false,
                        player
                ));
            }
            ItemStack hotbarItem = event.getView().getBottomInventory().getItem(hotbar);
            if (hotbarItem != null && !hotbarItem.isEmpty()) {
                output.add(eventFromStructure(
                        inventoryAccessible,
                        new ItemTransaction.LowerInventoryPosition(hotbar),
                        new ItemTransaction.RawPosition(event.getRawSlot()),
                        hotbarItem,
                        true,
                        player
                ));
            }
            return output;
        }
        if (CURSOR.contains(event.getAction())) {
            if (InventoryAction.SWAP_WITH_CURSOR == event.getAction()) {
                return List.of(
                        eventFromStructure(
                                inventoryAccessible,
                                new ItemTransaction.Cursor(),
                                new ItemTransaction.RawPosition(event.getRawSlot()),
                                event.getCursor(),
                                true,
                                player
                        ),
                        eventFromStructure(
                                inventoryAccessible,
                                new ItemTransaction.RawPosition(event.getRawSlot()),
                                new ItemTransaction.Cursor(),
                                event.getCurrentItem(),
                                false,
                                player
                        )
                );
            }
            if (ITEM_PICKUP_CURSOR.contains(event.getAction())) {
                return List.of(eventFromStructure(
                        inventoryAccessible,
                        new ItemTransaction.RawPosition(event.getRawSlot()),
                        new ItemTransaction.Cursor(),
                        event.getCurrentItem(),
                        false,
                        player
                ));
            }
            return List.of(eventFromStructure(
                    inventoryAccessible,
                    new ItemTransaction.Cursor(),
                    new ItemTransaction.RawPosition(event.getRawSlot()),
                    event.getCursor(),
                    true,
                    player
            ));
        }
        return List.of();
    }

    private List<ItemTransaction.InventoryPosition> findEmptyPositions(InventoryView view, @Nullable ItemStack currentItem, boolean topInventory) {
        if (currentItem == null) {
            return List.of();
        }
        Inventory inventory = topInventory ? view.getTopInventory() : view.getBottomInventory();
        int amount = currentItem.getAmount();
        int size = view.getTopInventory().getSize() + 9 * 4;
        List<ItemTransaction.InventoryPosition> positions = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int rawPos;
            if (topInventory) {
                rawPos = i;
            } else {
                rawPos = size - 1 - i;
            }
            if (inventory != view.getInventory(rawPos)) {
                continue;
            }
            ItemStack item = view.getItem(rawPos);
            if (item == null || item.isEmpty()) {
                positions.add(new ItemTransaction.RawPosition(rawPos));
                return positions;
            }
            if (!item.isSimilar(currentItem) || item.getMaxStackSize() <= item.getAmount()) {
                continue;
            }
            positions.add(new ItemTransaction.RawPosition(rawPos));
            amount -= item.getMaxStackSize() - item.getAmount();
            if (amount <= 0) {
                break;
            }
        }
        return positions;
    }

    private static ItemTransactionEvent<?> eventFromStructure(InventoryAccessible<ItemStack, Inventory> inventoryAccessible,
                                                              ItemTransaction.InventoryPosition from, ItemTransaction.InventoryPosition to,
                                                              ItemStack item, boolean insertion, @Nullable Player player) {
        ItemTransaction transaction = new ItemTransaction(from, to, item, insertion);
        Optional<Brew> brewOptional = BrewAdapter.fromItem(item);
        if (inventoryAccessible instanceof BukkitDistillery distillery) {
            return insertion ? new DistilleryInsertEvent(
                    distillery,
                    new ItemTransactionSession<>(transaction, brewOptional
                            .map(brew -> new ItemSource.BrewBasedSource(brew, new Brew.State.Brewing()))
                            .orElse(null)
                    ),
                    brewOptional.isEmpty(),
                    player
            ) : new DistilleryExtractEvent(
                    distillery,
                    new ItemTransactionSession<>(transaction, brewOptional
                            .map(brew -> BrewAdapter.toItem(brew, new Brew.State.Other()))
                            .map(ItemSource.ItemBasedSource::new)
                            .orElse(null)),
                    false,
                    player
            );
        }
        if (inventoryAccessible instanceof BukkitBarrel barrel) {
            return insertion ? new BarrelInsertEvent(
                    barrel,
                    new ItemTransactionSession<>(transaction, brewOptional
                            .map(brew -> new ItemSource.BrewBasedSource(brew, new Brew.State.Other()))
                            .orElse(null)
                    ),
                    brewOptional.isEmpty(),
                    player
            ) : new BarrelExtractEvent(
                    barrel,
                    new ItemTransactionSession<>(transaction, new ItemSource.ItemBasedSource(item)),
                    false,
                    player
            );
        }
        throw new IllegalStateException("Unknown structure: " + inventoryAccessible);
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