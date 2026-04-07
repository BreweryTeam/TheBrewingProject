package dev.jsinco.brewery.bukkit.integration.structure;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import dev.jsinco.brewery.bukkit.api.integration.StructureIntegration;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.util.ClassUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TownyIntegration implements StructureIntegration {

    private static final boolean ENABLED = ClassUtil.exists("com.palmergames.bukkit.towny.utils.PlayerCacheUtil");

    @Override
    public boolean hasAccess(Block block, Player player, BreweryKey type) {
        if (!ENABLED) {
            return true;
        }
        return PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.SWITCH);
    }

    @Override
    public boolean isEnabled() {
        return ENABLED;
    }

    @Override
    public String getId() {
        return "towny";
    }

    @Override
    public void onEnable() {}

}
