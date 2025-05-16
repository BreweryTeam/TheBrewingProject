package dev.jsinco.brewery.bukkit.listeners;

import dev.jsinco.brewery.bukkit.effect.event.NamedDrunkEventExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityEventListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(NamedDrunkEventExecutor.NO_DROPS)) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }
}
