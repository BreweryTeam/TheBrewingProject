package dev.jsinco.brewery.migrator

import com.google.common.base.Preconditions
import dev.jsinco.brewery.bukkit.TheBrewingProject
import dev.jsinco.brewery.migrator.migration.configuration.ConfigMigration
import dev.jsinco.brewery.migrator.migration.configuration.RecipeMigration
import dev.jsinco.brewery.migrator.migration.world.BarrelMigration
import dev.jsinco.brewery.migrator.migration.world.CauldronMigration
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*

class TbpMigratorPlugin : JavaPlugin() {
    private var loadSuccess = false
    private var hasMigrated = true

    override fun onLoad() {
        this.hasMigrated = loadMigrator()
        if (hasMigrated) {
            logger.severe { "The migration has already been run, shutting down the server!" }
            logger.info { "You need to remove both BreweryX and this migrator plugin from your server." }
            Bukkit.getServer().shutdown()
            return
        }
        RecipeMigration.migrateRecipes(
            File(dataFolder.parent, "BreweryX"),
            File(dataFolder.parent, "TheBrewingProject")
        )
        ConfigMigration.migrateConfig(
            File(dataFolder.parent, "BreweryX"),
            File(dataFolder.parent, "TheBrewingProject")
        )
        this.loadSuccess = true
    }

    private fun loadMigrator(): Boolean {
        val resource = "migration-state.properties"
        val file = File(dataFolder, resource)
        if (!file.exists()) {
            super.saveResource(resource, false)
        }
        val state = Properties()
        InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8).use {
            state.load(it)
            if (state["has-migrated"]?.equals("true") ?: false) {
                return true
            }
            state["has-migrated"] = "true"
        }
        OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8).use {
            state.store(it, "This represents the state of the migration plugin")
        }
        return false
    }

    override fun onEnable() {
        if (hasMigrated) {
            return
        }
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