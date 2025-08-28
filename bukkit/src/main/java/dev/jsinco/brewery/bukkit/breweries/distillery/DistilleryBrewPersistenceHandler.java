package dev.jsinco.brewery.bukkit.breweries.distillery;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.brew.BukkitDistilleryBrewDataType;
import dev.jsinco.brewery.bukkit.breweries.BrewInventory;
import dev.jsinco.brewery.bukkit.breweries.BrewPersistenceHandler;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.api.util.Pair;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DistilleryBrewPersistenceHandler implements BrewPersistenceHandler {


    private final BreweryLocation unique;
    private final boolean distillate;

    public DistilleryBrewPersistenceHandler(BreweryLocation unique, boolean distillate) {
        this.unique = unique;
        this.distillate = distillate;
    }


    /**
     * Set an item in the inventory and store the changes in the database
     *
     * @param brew
     * @param position
     */
    @Override
    public void store(@Nullable Brew brew, int position, @NotNull BrewInventory inventory) {
        if (Objects.equals(inventory.getBrews()[position], brew)) {
            return;
        }
        try {
            Brew previous = inventory.getBrews()[position];
            BukkitDistilleryBrewDataType.DistilleryContext context = contextProvider(position);
            Pair<Brew, BukkitDistilleryBrewDataType.DistilleryContext> data = new Pair<>(brew, context);
            if (previous == null) {
                TheBrewingProject.getInstance().getDatabase().insertValue(BukkitDistilleryBrewDataType.INSTANCE, data);
                return;
            }
            if (brew == null) {
                TheBrewingProject.getInstance().getDatabase().remove(BukkitDistilleryBrewDataType.INSTANCE, data);
                return;
            }
            TheBrewingProject.getInstance().getDatabase().updateValue(BukkitDistilleryBrewDataType.INSTANCE, data);
        } catch (PersistenceException e) {
            Logger.logErr(e);
        }

    }

    private BukkitDistilleryBrewDataType.DistilleryContext contextProvider(int position) {
        return new BukkitDistilleryBrewDataType.DistilleryContext(unique.x(), unique.y(), unique.z(), unique.worldUuid(), position, distillate);
    }
}
