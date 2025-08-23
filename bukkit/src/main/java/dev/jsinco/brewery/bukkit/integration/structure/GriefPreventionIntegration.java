package dev.jsinco.brewery.bukkit.integration.structure;

import dev.jsinco.brewery.bukkit.integration.StructureIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class GriefPreventionIntegration implements StructureIntegration {

    private static final boolean ENABLED = ClassUtil.exists("me.ryanhamshire.GriefPrevention.GriefPrevention");

    public boolean hasAccess(Block block, Player player) {
        if (!ENABLED) {
            return true;
        }
        GriefPrevention griefPrevention = GriefPrevention.instance;
        if (griefPrevention == null || !griefPrevention.isEnabled()) {
            return true;
        }
        PlayerData playerData = griefPrevention.dataStore.getPlayerData(player.getUniqueId());
        if (!griefPrevention.claimsEnabledForWorld(player.getWorld()) || playerData.ignoreClaims || !griefPrevention.config_claims_preventTheft) {
            return true;
        }
        // block container use during pvp combat
        if (playerData.inPvpCombat()) {
            return false;
        }
        // check permissions for the claim the Barrel is in
        Claim claim = griefPrevention.dataStore.getClaimAt(block.getLocation(), false, playerData.lastClaim);
        if (claim != null) {
            playerData.lastClaim = claim;
            Supplier<String> supplier = claim.checkPermission(player, ClaimPermission.Inventory, null);
            String noContainersReason = supplier != null ? supplier.get() : null;
            if (noContainersReason != null) {
                return false;
            }
        }
        // drop any pvp protection, as the player opens a barrel
        if (playerData.pvpImmune) {
            playerData.pvpImmune = false;
        }
        return true;
    }

    @Override
    public boolean enabled() {
        return ENABLED;
    }

    @Override
    public String getId() {
        return "griefprevention";
    }

    @Override
    public void enable() {}

}
