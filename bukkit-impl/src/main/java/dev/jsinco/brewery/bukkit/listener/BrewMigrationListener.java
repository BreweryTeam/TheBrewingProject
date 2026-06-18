package dev.jsinco.brewery.bukkit.listener;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.bukkit.brew.BrewAdapterAccess;
import dev.jsinco.brewery.configuration.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class BrewMigrationListener implements Listener {

    /**
     * Migrates an ItemStack from whatever encryption method/key
     * was used to 256 bit AES-GCM using the newest secret key.
     */
    private Optional<ItemStack> migrateItemStack(@Nullable ItemStack item) {
        if (item == null || item.isEmpty()) return Optional.empty();
        Optional<Brew> brewOptional = BrewAdapterAccess.fromItem(item);
        return brewOptional.map(brew -> BrewAdapterAccess.toItem(brew, new Brew.State.Other()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Config.config().reencryptItemsInInventories()) return;
        Inventory inventory = event.getPlayer().getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            final int finalSlot = slot;
            migrateItemStack(inventory.getItem(slot))
                    .ifPresent(itemStack -> inventory.setItem(finalSlot, itemStack));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        if (!Config.config().reencryptItemsInInventories()) return;
        Inventory inventory = event.getInventory();
        if (inventory.getType() == InventoryType.PLAYER) return;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            final int finalSlot = slot;
            migrateItemStack(inventory.getItem(slot))
                    .ifPresent(itemStack -> inventory.setItem(finalSlot, itemStack));
        }
    }
}
