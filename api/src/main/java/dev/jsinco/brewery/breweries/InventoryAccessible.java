package dev.jsinco.brewery.breweries;

import dev.jsinco.brewery.vector.BreweryLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface InventoryAccessible<IS, I> {

    boolean open(@NotNull BreweryLocation breweryLocation, @NotNull UUID playerUuid);

    void close(boolean silent);

    boolean inventoryAllows(@NotNull UUID playerUuid, @NotNull IS item);

    boolean inventoryAllows(@NotNull IS item);

    Set<I> getInventories();

    void tickInventory();

    Optional<I> access(@NotNull BreweryLocation breweryLocation);
}
