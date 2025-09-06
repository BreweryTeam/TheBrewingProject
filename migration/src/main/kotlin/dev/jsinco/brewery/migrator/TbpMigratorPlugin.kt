package dev.jsinco.brewery.migrator

import com.google.common.base.Preconditions
import dev.jsinco.brewery.bukkit.TheBrewingProject
import dev.jsinco.brewery.migrator.migration.configuration.RecipeMigration
import dev.jsinco.brewery.migrator.migration.world.BarrelMigration
import dev.jsinco.brewery.migrator.migration.world.CauldronMigration
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class TbpMigratorPlugin : JavaPlugin() {
    private var loadSuccess = false

    override fun onLoad() {
        RecipeMigration.migrateRecipes(
            File(dataFolder.parent, "BreweryX"),
            File(dataFolder.parent, "TheBrewingProject")
        )
        this.loadSuccess = true
    }

    override fun onEnable() {
        Preconditions.checkState(loadSuccess, "Failed on load, check on load logs!")
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