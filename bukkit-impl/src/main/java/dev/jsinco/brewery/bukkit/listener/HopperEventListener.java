package dev.jsinco.brewery.bukkit.listener;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.breweries.InventoryAccessible;
import dev.jsinco.brewery.api.structure.PlacedStructureRegistry;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.event.transaction.ItemTransactionEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransaction;
import dev.jsinco.brewery.bukkit.brew.BrewAdapterAccess;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.configuration.features.FeatureFlag;
import dev.jsinco.brewery.configuration.features.FeaturesConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public record HopperEventListener(PlacedStructureRegistry placedStructureRegistry,
                                  BreweryRegistry registry) implements Listener {


    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Optional<InventoryAccessible<ItemStack, Inventory>> source = Optional.ofNullable(registry.getFromInventory(event.getSource()));
        Optional<InventoryAccessible<ItemStack, Inventory>> destination = Optional.ofNullable(registry.getFromInventory(event.getDestination()));
        Optional<InventoryAccessible<ItemStack, Inventory>> both = destination.or(() -> source);
        both.filter(inventoryAccessible -> !inventoryAccessible.inventoryAllows(event.getItem()))
                .ifPresent(ignored -> event.setCancelled(true));
        source.flatMap(ignored -> BrewAdapterAccess.fromItem(event.getItem())
                        .map(brew -> BrewAdapterAccess.toItem(brew, new Brew.State.Other())))
                .ifPresent(event::setItem);

    }


    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent dragEvent) {
        if (!FeaturesConfig.test(FeatureFlag.BREW_MAKING, dragEvent.getWhoClicked().getWorld().getName())) {
            return;
        }
        InventoryAccessible<ItemStack, Inventory> inventoryAccessible = registry.getFromInventory(dragEvent.getInventory());
        if (inventoryAccessible == null) {
            return;
        }
        InventoryView inventoryView = dragEvent.getView();
        List<? extends ItemTransactionEvent<?>> transactionEvents = dragEvent.getNewItems()
                .entrySet()
                .stream()
                .filter(entry -> dragEvent.getInventory() == inventoryView.getInventory(entry.getKey()))
                .map(entry -> InventoryListenerUtil.eventFromStructure(
                        inventoryAccessible,
                        new ItemTransaction.Cursor(),
                        new ItemTransaction.RawPosition(entry.getKey()),
                        entry.getValue(),
                        true,
                        dragEvent.getWhoClicked() instanceof Player player ? player : null
                )).toList();
        List<dev.jsinco.brewery.api.util.CancelState> cancelled = transactionEvents.stream()
                .filter(transactionEvent -> !transactionEvent.callEvent())
                .map(ItemTransactionEvent::getCancelState)
                .toList();
        if (!cancelled.isEmpty()) {
            cancelled.stream()
                    .filter(dev.jsinco.brewery.api.util.CancelState.PermissionDenied.class::isInstance)
                    .map(dev.jsinco.brewery.api.util.CancelState.PermissionDenied.class::cast)
                    .map(dev.jsinco.brewery.api.util.CancelState.PermissionDenied::message)
                    .forEach(dragEvent.getWhoClicked()::sendMessage);
            dragEvent.setCancelled(true);
            return;
        }

        dragEvent.getWhoClicked().getScheduler().run(TheBrewingProject.getInstance(), ignored ->
                        InventoryListenerUtil.displayEventResult(inventoryView, transactionEvents),
                null
        );
    }
}
