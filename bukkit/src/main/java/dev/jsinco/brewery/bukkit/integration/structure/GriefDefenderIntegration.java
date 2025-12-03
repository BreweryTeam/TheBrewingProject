package dev.jsinco.brewery.bukkit.integration.structure;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.bukkit.api.integration.StructureIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GriefDefenderIntegration implements StructureIntegration {
    @Override
    public boolean hasAccess(Block block, Player player, BreweryKey type) {
        Claim claim = GriefDefender.getCore().getClaimAt(block.getLocation());
        return claim == null || claim.isUserTrusted(player.getUniqueId(), TrustTypes.CONTAINER);
    }

    @Override
    public String getId() {
        return "griefdefender";
    }

    @Override
    public boolean isEnabled() {
        return ClassUtil.exists("com.griefdefender.api.GriefDefender");
    }
}
