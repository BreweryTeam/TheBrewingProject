package dev.jsinco.brewery.bukkit.breweries.barrel;

import dev.jsinco.brewery.brew.BarrelBrewDataType;
import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BukkitBarrelBrewDataType;
import dev.jsinco.brewery.bukkit.breweries.BrewInventory;
import dev.jsinco.brewery.bukkit.breweries.BrewPersistenceHandler;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BarrelBrewPersistenceHandler implements BrewPersistenceHandler {

    private final BreweryLocation unique;

    public BarrelBrewPersistenceHandler(BreweryLocation unique) {
        this.unique = unique;
    }

    @Override
    public void store(@Nullable Brew brew, int position, @NotNull BrewInventory inventory) {
        if (Objects.equals(inventory.getBrews()[position], brew)) {
            return;
        }
        try {
            Brew previous = inventory.getBrews()[position];
            BarrelBrewDataType.BarrelContext context = new BarrelBrewDataType.BarrelContext(unique.x(), unique.y(), unique.z(), position, unique.worldUuid());
            Pair<Brew, BarrelBrewDataType.BarrelContext> data = new Pair<>(brew, context);
            if (previous == null) {
                TheBrewingProject.getInstance().getDatabase().insertValue(BukkitBarrelBrewDataType.INSTANCE, data);
                return;
            }
            if (brew == null) {
                TheBrewingProject.getInstance().getDatabase().remove(BukkitBarrelBrewDataType.INSTANCE, data);
                return;
            }
            TheBrewingProject.getInstance().getDatabase().updateValue(BukkitBarrelBrewDataType.INSTANCE, data);
        } catch (PersistenceException e) {
            Logger.logErr(e);
        }
    }
}
