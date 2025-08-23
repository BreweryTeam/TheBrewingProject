package dev.jsinco.brewery.bukkit.integration.structure;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.integration.StructureIntegration;
import dev.jsinco.brewery.bukkit.util.BukkitAdapter;
import dev.jsinco.brewery.structure.MultiblockStructure;
import dev.jsinco.brewery.util.ClassUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.popcraft.bolt.BoltAPI;

import java.util.List;
import java.util.Optional;

public class BoltIntegration implements StructureIntegration {

    private static final boolean ENABLED = ClassUtil.exists("org.popcraft.bolt.BoltAPI");
    private static BoltAPI boltAPI;

    public boolean hasAccess(Block block, Player player) {
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

    @Override
    public boolean enabled() {
        return ENABLED;
    }

    @Override
    public String getId() {
        return "bolt";
    }

    @Override
    public void enable() {
        if (!ENABLED) {
            return;
        }
        boltAPI = Bukkit.getServer().getServicesManager().load(BoltAPI.class);
    }

}
