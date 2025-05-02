package dev.jsinco.brewery.migrator.listener

import dev.jsinco.brewery.bukkit.TheBrewingProject
import dev.jsinco.brewery.migrator.barrel.BarrelMigration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent

object WorldListener : Listener {

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        BarrelMigration.migrateWorld(event.world)
        TheBrewingProject.getInstance().reload()
    }
}