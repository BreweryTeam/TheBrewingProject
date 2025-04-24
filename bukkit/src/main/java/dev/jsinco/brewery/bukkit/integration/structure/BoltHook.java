package dev.jsinco.brewery.bukkit.integration.structure;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.structure.MultiblockStructure;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.popcraft.bolt.BoltAPI;

import java.util.List;
import java.util.Optional;

public class BoltHook {

    private static final boolean ENABLED = checkAvailable();
    private static BoltAPI boltAPI;

    private static boolean checkAvailable() {
        try {
            Class.forName("org.popcraft.bolt.BoltAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean hasAccess(Block block, Player player) {
        if (!ENABLED) {
            return true;
        }
        if (boltAPI == null) {
            return true;
        }
        Optional<MultiblockStructure<?>> multiBlockStructureOptional = TheBrewingProject.getInstance().getPlacedStructureRegistry().getStructure(BukkitAdapter.toBreweryLocation(block));
        return multiBlockStructureOptional
                .stream()
                .map(MultiblockStructure::positions)
                .flatMap(List::stream)
                .map(BukkitAdapter::toBlock)
                .allMatch(position -> boltAPI.canAccess(position, player))
                && boltAPI.canAccess(block, player);
    }

    public static void initiate() {
        if (!ENABLED) {
            return;
        }
        boltAPI = Bukkit.getServer().getServicesManager().load(BoltAPI.class);
    }
}
