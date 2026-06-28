package dev.jsinco.brewery.bukkit.database.barrel;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import dev.jsinco.brewery.bukkit.breweries.barrel.BukkitBarrel;
import dev.jsinco.brewery.database.Session;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BarrelSession extends Session<BarrelSession> {

    CompletableFuture<Void> insertBrew(BreweryLocation barrelLocation, int inventoryPos, Brew brew);

    CompletableFuture<Void> removeBrew(BreweryLocation barrelLocation, int inventoryPos);

    CompletableFuture<List<BrewLookupResult>> findBrews(BreweryLocation barrelLocation);

    CompletableFuture<Void> updateBrew(BreweryLocation barrelLocation, int inventoryPos, Brew newBrew);

    CompletableFuture<Void> insertBarrel(BukkitBarrel barrel);

    CompletableFuture<Void> removeBarrel(BukkitBarrel barrel);

    CompletableFuture<List<BukkitBarrel>> findBarrels(UUID worldUuid);

    record BrewLookupResult(Brew brew, int position) {
    }
}
