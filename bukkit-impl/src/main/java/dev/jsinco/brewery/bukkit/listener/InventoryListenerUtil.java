package dev.jsinco.brewery.bukkit.listener;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.breweries.InventoryAccessible;
import dev.jsinco.brewery.bukkit.api.event.transaction.BarrelExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.BarrelInsertEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.DistilleryExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.DistilleryInsertEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.ItemTransactionEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransaction;
import dev.jsinco.brewery.bukkit.api.transaction.ItemTransactionSession;
import dev.jsinco.brewery.bukkit.brew.BrewAdapterAccess;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistillery;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class InventoryListenerUtil {

    private InventoryListenerUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void displayEventResult(@NonNull InventoryView view, List<? extends ItemTransactionEvent<?>> transactions) {
        for (ItemTransactionEvent<?> transactionEvent : transactions) {
            ItemTransactionSession<?> session = transactionEvent.getTransactionSession();
            ItemTransaction transaction = session.getTransaction();
            ItemStack itemStack = session.getResult() == null ? null : session.getResult().get();
            if (transaction.to() instanceof ItemTransaction.Cursor
                    && transaction.itemStack().equals(view.getCursor())
            ) {
                view.setCursor(itemStack);
            }
            if (transaction.to() instanceof ItemTransaction.RawPosition(int pos)
                    && transaction.itemStack().equals(view.getItem(pos))
            ) {
                view.setItem(pos, itemStack);
            }
            if (transaction.to() instanceof ItemTransaction.UpperInventoryPosition(int pos)
                    && transaction.itemStack().equals(view.getTopInventory().getItem(pos))
            ) {
                view.getTopInventory().setItem(pos, itemStack);
            }
            if (transaction.to() instanceof ItemTransaction.LowerInventoryPosition(int pos)
                    && transaction.itemStack().equals(view.getBottomInventory().getItem(pos))
            ) {
                view.getBottomInventory().setItem(pos, itemStack);
            }
        }
    }

    public static ItemTransactionEvent<?> eventFromStructure(InventoryAccessible<ItemStack, Inventory> inventoryAccessible,
                                                             ItemTransaction.InventoryPosition from, ItemTransaction.InventoryPosition to,
                                                             ItemStack item, boolean insertion, @Nullable Player player) {
        ItemTransaction transaction = new ItemTransaction(from, to, item, insertion);
        Optional<Brew> brewOptional = BrewAdapterAccess.fromItem(item)
                .map(inventoryAccessible::initializeBrew);
        if (player != null) {
            brewOptional = brewOptional
                    .map(brew -> brew.witModifiedLastStep(step ->
                            step instanceof BrewingStep.AuthoredStep<?> authoredStep && !authoredStep.isCompleted()
                                    ? authoredStep.withBrewersReplaced(new LinkedList<>()) : step
                    ))
                    .map(brew -> brew.witModifiedLastStep(step ->
                            step instanceof BrewingStep.AuthoredStep<?> authoredStep
                                    ? authoredStep.withBrewer(player.getUniqueId()) : step
                    ));
        }
        if (inventoryAccessible instanceof BukkitDistillery distillery) {
            dev.jsinco.brewery.api.util.CancelState cancelState = brewOptional.isEmpty() ? new dev.jsinco.brewery.api.util.CancelState.Cancelled() :
                    player == null || player.hasPermission("brewery.distillery.access") ? new dev.jsinco.brewery.api.util.CancelState.Allowed() :
                    new dev.jsinco.brewery.api.util.CancelState.PermissionDenied(Component.translatable("tbp.distillery.access-denied"));
            return insertion ? new DistilleryInsertEvent(
                    distillery,
                    new ItemTransactionSession<>(transaction, brewOptional
                                                              .map(brew -> new ItemSource.BrewBasedSource(brew, new Brew.State.Brewing()))
                                                              .orElse(null)
                    ),
                    cancelState,
                    player
            ) : new DistilleryExtractEvent(
                    distillery,
                    new ItemTransactionSession<>(transaction, brewOptional
                                                              .map(brew -> BrewAdapterAccess.toItem(brew, new Brew.State.Other()))
                                                              .map(ItemSource.ItemBasedSource::new)
                                                              .orElse(null)
                    ),
                    cancelState,
                    player
            );
        }
        if (inventoryAccessible instanceof BukkitBarrel barrel) {
            dev.jsinco.brewery.api.util.CancelState cancelState = brewOptional.isEmpty() ? new dev.jsinco.brewery.api.util.CancelState.Cancelled() :
                    player == null || player.hasPermission("brewery.barrel.access") ? new dev.jsinco.brewery.api.util.CancelState.Allowed() :
                    new dev.jsinco.brewery.api.util.CancelState.PermissionDenied(Component.translatable("tbp.barrel.access-denied"));
            return insertion ? new BarrelInsertEvent(
                    barrel,
                    new ItemTransactionSession<>(transaction, brewOptional
                                                              .map(brew -> new ItemSource.BrewBasedSource(brew, new Brew.State.Brewing()))
                                                              .orElse(null)
                    ),
                    cancelState,
                    player
            ) : new BarrelExtractEvent(
                    barrel,
                    new ItemTransactionSession<>(transaction, brewOptional
                                                              .map(brew -> BrewAdapterAccess.toItem(brew, new Brew.State.Other()))
                                                              .map(ItemSource.ItemBasedSource::new)
                                                              .orElse(null)),
                    cancelState,
                    player
            );
        }
        throw new IllegalStateException("Unknown structure: " + inventoryAccessible);
    }
}
