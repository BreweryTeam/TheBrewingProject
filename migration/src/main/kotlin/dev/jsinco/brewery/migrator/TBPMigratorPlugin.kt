package dev.jsinco.brewery.migrator

import dev.jsinco.brewery.bukkit.TheBrewingProject
import dev.jsinco.brewery.migrator.barrel.BarrelMigration
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class TBPMigratorPlugin : JavaPlugin() {

    override fun onEnable() {
        Bukkit.getWorlds().forEach(BarrelMigration::migrateWorld)
        TheBrewingProject.getInstance().database.flush().join()
        TheBrewingProject.getInstance().reload()
    }
}