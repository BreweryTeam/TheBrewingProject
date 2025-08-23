package dev.jsinco.brewery.bukkit.integration.structure;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.integration.StructureIntegration;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.util.ClassUtil;
import me.angeschossen.lands.api.flags.enums.FlagTarget;
import me.angeschossen.lands.api.flags.enums.RoleFlagCategory;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import me.angeschossen.lands.api.land.LandWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class LandsIntegration implements StructureIntegration {

    private static final boolean ENABLED = ClassUtil.exists("me.angeschossen.lands.api.LandsIntegration");
    private static me.angeschossen.lands.api.LandsIntegration landsIntegration;
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
    public boolean isEnabled() {
        return ENABLED;
    }

    @Override
    public String getId() {
        return "lands";
    }

    @Override
    public void onLoad() {
        if (!ENABLED) {
            return;
        }
        landsIntegration = me.angeschossen.lands.api.LandsIntegration.of(TheBrewingProject.getInstance());
        PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();
        String name = serializer.serialize(GlobalTranslator.render(Component
                .translatable("tbp.integration.lands.flag.barrel-access.name"), Config.config().language()));
        String[] description = serializer.serialize(GlobalTranslator.render(Component
                        .translatable("tbp.integration.lands.flag.barrel-access.description"), Config.config().language()))
                .split("\\\\n");
        barrelAccessFlag = RoleFlag.of(landsIntegration, FlagTarget.PLAYER, RoleFlagCategory.ACTION, "barrel_access")
                .setDisplayName(name)
                .setDescription(Arrays.stream(description).toList())
                .setIcon(new ItemStack(Material.BARREL))
                .setDisplay(true);
    }

}
