package dev.jsinco.brewery.migrator

import dev.jsinco.brewery.migrator.listener.WorldListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class TBPMigratorPlugin : JavaPlugin() {

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(WorldListener, this)
    }
}