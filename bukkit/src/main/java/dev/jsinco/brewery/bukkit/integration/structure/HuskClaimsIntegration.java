package dev.jsinco.brewery.bukkit.integration.structure;

import dev.jsinco.brewery.bukkit.api.integration.StructureIntegration;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.ClassUtil;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.libraries.cloplib.operation.OperationType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class HuskClaimsIntegration implements StructureIntegration {

    private static final boolean ENABLED = ClassUtil.exists("net.william278.huskclaims.api.BukkitHuskClaimsAPI");

    @Override
    public boolean hasAccess(Block block, Player player, BreweryKey type) {
        if (!ENABLED) {
            return true;
        }
        BukkitHuskClaimsAPI huskClaimsAPI = BukkitHuskClaimsAPI.getInstance();
        return huskClaimsAPI.isOperationAllowed(huskClaimsAPI.getOnlineUser(player), OperationType.CONTAINER_OPEN, huskClaimsAPI.getPosition(block.getLocation()));
    }

    @Override
    public boolean isEnabled() {
        return ENABLED;
    }

    @Override
    public String getId() {
        return "huskclaims";
    }

    @Override
    public void onEnable() {}

}
