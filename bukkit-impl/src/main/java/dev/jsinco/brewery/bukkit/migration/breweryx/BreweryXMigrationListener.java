package dev.jsinco.brewery.bukkit.migration.breweryx;

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

public class BreweryXMigrationListener implements Listener {

    private Optional<ItemStack> migrate(@Nullable ItemStack item) {
        if (item == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BreweryXMigrationUtils.migrate(item));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Config.config().migrateFromBreweryX()) {
            return;
        }
        Inventory inventory = event.getPlayer().getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            final int finalSlot = slot;
            migrate(inventory.getItem(finalSlot))
                    .ifPresent(itemStack -> inventory.setItem(finalSlot, itemStack));
        }
    }

    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        if (!Config.config().migrateFromBreweryX()) {
            return;
        }
        Inventory inventory = event.getInventory();
        if (inventory.getType() == InventoryType.PLAYER) {
            return;
        }
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            final int finalSlot = slot;
            migrate(inventory.getItem(finalSlot))
                    .ifPresent(itemStack -> inventory.setItem(finalSlot, itemStack));
        }
    }

}
