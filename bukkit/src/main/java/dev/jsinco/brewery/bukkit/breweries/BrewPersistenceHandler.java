package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.brew.Brew;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BrewPersistenceHandler {

    void store(@Nullable Brew brew, int position, @NotNull BrewInventory inventory);
}
