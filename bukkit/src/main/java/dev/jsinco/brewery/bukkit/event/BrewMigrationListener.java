package dev.jsinco.brewery.bukkit.event;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import dev.jsinco.brewery.configuration.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class BrewMigrationListener implements Listener {

    /**
     * Migrates an ItemStack from whatever encryption method/key
     * was used to 256 bit AES-GCM using the newest secret key.
     */
    private ItemStack migrateItemStack(ItemStack item) {

        if (item == null || item.isEmpty()) return item;
        Optional<Brew> brewOptional = BrewAdapter.fromItem(item);
        if (brewOptional.isEmpty()) return item;

        // This re-encrypts the brew using 256 bit AES-GCM and the latest key:
        return BrewAdapter.toItem(brewOptional.get(), new Brew.State.Other());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Config.config().reencryptItemsInInventories()) return;
        Inventory inventory = event.getPlayer().getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, migrateItemStack(inventory.getItem(slot)));
        }
    }

    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        if (!Config.config().reencryptItemsInInventories()) return;
        Inventory inventory = event.getInventory();
        if (inventory.getType() == InventoryType.PLAYER) return;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, migrateItemStack(inventory.getItem(slot)));
        }
    }
}
