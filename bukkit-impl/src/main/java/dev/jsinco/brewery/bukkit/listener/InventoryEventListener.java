package dev.jsinco.brewery.bukkit.listener;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewQuality;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.breweries.InventoryAccessible;
import dev.jsinco.brewery.bukkit.Statistics;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.event.transaction.BarrelExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.BarrelInsertEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.DistilleryExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.DistilleryInsertEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.ItemTransactionEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransaction;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import dev.jsinco.brewery.bukkit.brew.BrewAdapterAccess;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistillery;
import dev.jsinco.brewery.bukkit.effect.named.PukeNamedExecutable;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.configuration.features.FeatureFlag;
import dev.jsinco.brewery.configuration.features.FeaturesConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class InventoryEventListener implements Listener {

    private final BreweryRegistry registry;
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

    public InventoryEventListener(BreweryRegistry registry) {
        this.registry = registry;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!FeaturesConfig.test(FeatureFlag.BREW_MAKING, event.getWhoClicked().getWorld().getName())) {
            return;
        }
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
                if (transactionEvent.getCancelState() instanceof dev.jsinco.brewery.api.util.CancelState.PermissionDenied(
                        Component denyMessage
                )) {
                    event.getWhoClicked().sendMessage(denyMessage);
                }
                event.setResult(Event.Result.DENY);
                return;
            }
        }
        for (ItemTransactionEvent<?> transactionEvent : transactions) {
            if (transactionEvent instanceof DistilleryExtractEvent || transactionEvent instanceof BarrelExtractEvent) {
                ItemSource brewItemStack = transactionEvent.getTransactionSession().getResult();
                if (brewItemStack == null) {
                    continue;
                }
                Optional.ofNullable(brewItemStack.get().getPersistentDataContainer().get(BrewAdapterAccess.BREWERY_SCORE, PersistentDataType.DOUBLE))
                        .ifPresent(score -> Statistics.registerBrewMade(BrewQuality.quality(score).orElse(null)));
            }
        }
        event.getWhoClicked().getScheduler().run(TheBrewingProject.getInstance(), ignored ->
                        InventoryListenerUtil.displayEventResult(event.getView(), transactions),
                null
        );
    }

    private List<? extends ItemTransactionEvent<?>> compileTransactionsFromClick(InventoryClickEvent event, boolean upperInventoryIsClicked,
                                                                                 InventoryAccessible<ItemStack, Inventory> inventoryAccessible) {
        Player player = event.getWhoClicked() instanceof Player temp ? temp : null;
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (upperInventoryIsClicked) {
                return findEmptyPositions(event.getView(), event.getCurrentItem(), false)
                        .stream()
                        .map(inventoryPosition -> InventoryListenerUtil.eventFromStructure(
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
                        .map(inventoryPosition -> InventoryListenerUtil.eventFromStructure(
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
                output.add(InventoryListenerUtil.eventFromStructure(
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
                output.add(InventoryListenerUtil.eventFromStructure(
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
                        InventoryListenerUtil.eventFromStructure(
                                inventoryAccessible,
                                new ItemTransaction.Cursor(),
                                new ItemTransaction.RawPosition(event.getRawSlot()),
                                event.getCursor(),
                                true,
                                player
                        ),
                        InventoryListenerUtil.eventFromStructure(
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
                return List.of(InventoryListenerUtil.eventFromStructure(
                        inventoryAccessible,
                        new ItemTransaction.RawPosition(event.getRawSlot()),
                        new ItemTransaction.Cursor(),
                        event.getCurrentItem(),
                        false,
                        player
                ));
            }
            return List.of(InventoryListenerUtil.eventFromStructure(
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

    @EventHandler(ignoreCancelled = true)
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (event.getItem().getPersistentDataContainer().has(PukeNamedExecutable.PUKE_ITEM)) {
            event.setCancelled(true);
        }
    }
}