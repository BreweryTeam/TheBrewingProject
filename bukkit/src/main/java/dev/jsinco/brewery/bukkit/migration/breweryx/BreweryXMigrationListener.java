package dev.jsinco.brewery.bukkit.migration.breweryx;

import dev.jsinco.brewery.configuration.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BreweryXMigrationListener implements Listener {

    private @Nullable ItemStack migrate(@Nullable ItemStack item) {
        if (item == null) return null;
        ItemStack migrated = BreweryXMigrationUtils.migrate(item);
        if (migrated == null) return item;
        return migrated;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Config.config().migrateFromBreweryX()) return;
        Inventory inventory = event.getPlayer().getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if(itemStack == null) {
                continue;
            }
            inventory.setItem(slot, migrate(itemStack));
        }
    }

    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event) {
        if (!Config.config().migrateFromBreweryX()) return;
        Inventory inventory = event.getInventory();
        if (inventory.getType() == InventoryType.PLAYER) return;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if(itemStack == null) {
                continue;
            }
            inventory.setItem(slot, migrate(itemStack));
        }
    }

}
