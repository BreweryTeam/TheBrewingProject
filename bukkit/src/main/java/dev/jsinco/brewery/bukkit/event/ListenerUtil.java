package dev.jsinco.brewery.bukkit.event;

import dev.jsinco.brewery.bukkit.breweries.BreweryRegistry;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldronDataType;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.sql.Database;
import dev.jsinco.brewery.structure.SinglePositionStructure;
import dev.jsinco.brewery.util.Logger;
import org.jetbrains.annotations.NotNull;

public class ListenerUtil {

    public static void removeActiveSinglePositionStructure(@NotNull SinglePositionStructure structure, BreweryRegistry registry, Database database) {
        registry.removeActiveSinglePositionStructure(structure);
        if (structure instanceof BukkitCauldron cauldron) {
            try {
                database.remove(BukkitCauldronDataType.INSTANCE, cauldron);
            } catch (PersistenceException e) {
                Logger.logErr(e);
            }
        }
    }
}
