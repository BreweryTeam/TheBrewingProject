package dev.jsinco.brewery.bukkit.breweries;

import dev.jsinco.brewery.api.brew.Brew;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface BrewPersistenceHandler {

    void store(@Nullable Brew brew, int position, @NonNull BrewInventoryImpl inventory);
}
