package dev.jsinco.brewery.bukkit.integration.structure;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.jsinco.brewery.bukkit.api.integration.StructureIntegration;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.util.ClassUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class WorldGuardIntegration implements StructureIntegration {

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("com.sk89q.worldguard.WorldGuard");
    }

    @Override
    public String getId() {
        return "worldguard";
    }

    @Override
    public boolean hasAccess(Block block, Player player, BreweryKey type) {
        WorldGuard worldGuard = WorldGuard.getInstance();
        WorldGuardPlugin instance = WorldGuardPlugin.inst();
        if (worldGuard == null || instance == null || !instance.isEnabled()) {
            return true;
        }
        WorldGuardPlatform platform = worldGuard.getPlatform();
        RegionQuery query = platform.getRegionContainer().createQuery();

        return query.testBuild(BukkitAdapter.adapt(block.getLocation()), instance.wrapPlayer(player), Flags.USE, Flags.CHEST_ACCESS);
    }

}
