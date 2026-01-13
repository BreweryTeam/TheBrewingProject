package dev.jsinco.brewery.bukkit.listener;

import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldronDataType;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrelDataType;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistillery;
import dev.jsinco.brewery.bukkit.breweries.distillery.BukkitDistilleryDataType;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.structure.PlacedStructureRegistryImpl;
import dev.jsinco.brewery.util.FutureUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class WorldEventListener implements Listener {

    private final Database database;
    private final PlacedStructureRegistryImpl placedStructureRegistry;
    private final BreweryRegistry registry;
    private final Executor globalThread = runnable -> Bukkit.getGlobalRegionScheduler().run(TheBrewingProject.getInstance(), ignored -> runnable.run());

    public WorldEventListener(Database database, PlacedStructureRegistryImpl placedStructureRegistry, BreweryRegistry registry) {
        this.database = database;
        this.placedStructureRegistry = placedStructureRegistry;
        this.registry = registry;
    }

    public void init() {
        Bukkit.getServer().getWorlds().forEach(this::loadWorld);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent event) {
        loadWorld(event.getWorld());
    }

    public void onWorldUnload(WorldUnloadEvent event) {
        placedStructureRegistry.unloadWorld(event.getWorld().getUID());
    }

    private void loadWorld(World world) {
        try {
            CompletableFuture<List<BukkitBarrel>> barrelsFuture = database.find(BukkitBarrelDataType.INSTANCE, world.getUID());
            CompletableFuture<List<BukkitCauldron>> cauldronsFuture = database.find(BukkitCauldronDataType.INSTANCE, world.getUID());
            CompletableFuture<List<BukkitDistillery>> distilleriesFuture = database.find(BukkitDistilleryDataType.INSTANCE, world.getUID());
            CompletableFuture.allOf(barrelsFuture, distilleriesFuture, cauldronsFuture)
                    .thenAcceptAsync(ignored -> {
                        for (BukkitBarrel barrel : barrelsFuture.join()) {
                            placedStructureRegistry.registerStructure(barrel.getStructure());
                            registry.registerInventory(barrel);
                        }
                        cauldronsFuture.join().forEach(registry::addActiveSinglePositionStructure);
                        for(BukkitDistillery distillery : distilleriesFuture.join()) {
                            placedStructureRegistry.registerStructure(distillery.getStructure());
                            registry.registerInventory(distillery);
                        }
                    }, globalThread);

        } catch (PersistenceException e) {
            Logger.logErr(e);
        }
    }
}
