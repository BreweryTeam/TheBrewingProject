package dev.jsinco.brewery.bukkit.listener;

import dev.jsinco.brewery.api.structure.SinglePositionStructure;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.breweries.BukkitCauldron;
import dev.jsinco.brewery.bukkit.database.SessionTypes;
import dev.jsinco.brewery.database.PersistenceException;
import org.jspecify.annotations.NonNull;

public class ListenerUtil {

    public static void removeActiveSinglePositionStructure(@NonNull SinglePositionStructure structure) {
        structure.destroy();
        TheBrewingProject.getInstance().getBreweryRegistry().removeActiveSinglePositionStructure(structure);
        if (structure instanceof BukkitCauldron cauldron) {
            try {
                TheBrewingProject.getInstance().getDatabase().startSession(SessionTypes.CAULDRON_SESSION_TYPE)
                        .removeCauldron(cauldron)
                        .exceptionally(Logger::logAndTrackErr);
            } catch (PersistenceException e) {
                Logger.logErr(e);
            }
        }
    }
}
