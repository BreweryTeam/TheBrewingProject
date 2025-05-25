package dev.jsinco.brewery.bukkit.integration.structure;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.integration.StructureIntegration;
import dev.jsinco.brewery.util.ClassUtil;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.enums.FlagTarget;
import me.angeschossen.lands.api.flags.enums.RoleFlagCategory;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import me.angeschossen.lands.api.land.LandWorld;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LandsHook implements StructureIntegration {

    private static final boolean ENABLED = ClassUtil.exists("me.angeschossen.lands.api.LandsIntegration");
    private static LandsIntegration landsIntegration;
    private static RoleFlag barrelAccessFlag;


    public boolean hasAccess(Block block, Player player) {
        if (!ENABLED) {
            return true;
        }
        LandWorld lWorld = landsIntegration.getWorld(block.getWorld());
        if (lWorld == null) {
            return true;
        }
        return lWorld.hasRoleFlag(player.getUniqueId(), block.getLocation(), barrelAccessFlag);
    }

    @Override
    public boolean enabled() {
        return ENABLED;
    }

    @Override
    public String getId() {
        return "lands";
    }

    @Override
    public void initialize() {
        if (!ENABLED) {
            return;
        }
        landsIntegration = LandsIntegration.of(
                TheBrewingProject.getInstance()
        );
        barrelAccessFlag = RoleFlag.of(landsIntegration, FlagTarget.PLAYER, RoleFlagCategory.ACTION, "barrel_access")
                .setDisplayName("Barrel Access")
                .setDescription(List.of("§r§7Allows opening", "§r§7BreweryX barrels."))
                .setIcon(new ItemStack(Material.BARREL))
                .setDisplay(true);
    }
}
