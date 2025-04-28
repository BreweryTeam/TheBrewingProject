package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.brew.Brew;
import org.jetbrains.annotations.NotNull;

public interface BrewPersistenceHandler {

    void store(@NotNull Brew brew, int position, @NotNull BrewInventory inventory);
}
