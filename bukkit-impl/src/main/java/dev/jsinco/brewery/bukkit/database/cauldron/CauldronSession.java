package dev.jsinco.brewery.bukkit.database.cauldron;

import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import dev.jsinco.brewery.database.Session;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CauldronSession extends Session<CauldronSession> {

    CompletableFuture<Void> insertCauldron(BukkitCauldron cauldron);

    CompletableFuture<Void> updateCauldron(BukkitCauldron newCauldron);

    CompletableFuture<Void> removeCauldron(BukkitCauldron cauldron);

    CompletableFuture<List<BukkitCauldron>> findCauldrons(UUID worldUuid);
}
