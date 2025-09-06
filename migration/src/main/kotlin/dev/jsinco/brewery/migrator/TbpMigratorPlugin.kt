package dev.jsinco.brewery.migrator

import dev.jsinco.brewery.bukkit.TheBrewingProject
import dev.jsinco.brewery.migrator.migration.BarrelMigration
import dev.jsinco.brewery.migrator.migration.CauldronMigration
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class TbpMigratorPlugin : JavaPlugin() {

    override fun onEnable() {
        Bukkit.getScheduler().runTask(this) { ->
            Bukkit.getWorlds().forEach(BarrelMigration::migrateWorld)
            Bukkit.getWorlds().forEach(CauldronMigration::migrateWorld)
            TheBrewingProject.getInstance().database.flush().join()
            TheBrewingProject.getInstance().reload()
            val pluginManager = Bukkit.getPluginManager()
            logger.info("Successfully migrated barrel and configuration data to TheBrewingProject")
            logger.info("You can now remove both BreweryX and " + this.name + " from your plugins folder")
            pluginManager.disablePlugin(pluginManager.getPlugin("BreweryX")!!)
            pluginManager.disablePlugin(this)
        }
    }
}