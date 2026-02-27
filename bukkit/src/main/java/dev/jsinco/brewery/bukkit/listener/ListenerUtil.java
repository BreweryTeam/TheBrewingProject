package dev.jsinco.brewery.bukkit.listener;

import dev.jsinco.brewery.api.structure.SinglePositionStructure;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldronDataType;
import dev.jsinco.brewery.database.PersistenceException;
import org.jetbrains.annotations.NotNull;

public class ListenerUtil {

    public static void removeActiveSinglePositionStructure(@NotNull SinglePositionStructure structure) {
        structure.destroy();
        TheBrewingProject.getInstance().getBreweryRegistry().removeActiveSinglePositionStructure(structure);
        if (structure instanceof BukkitCauldron cauldron) {
            try {
                TheBrewingProject.getInstance().getDatabase().remove(BukkitCauldronDataType.INSTANCE, cauldron);
            } catch (PersistenceException e) {
                Logger.logErr(e);
            }
        }
    }
}
