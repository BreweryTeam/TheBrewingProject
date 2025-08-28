package dev.jsinco.brewery.bukkit.integration.structure;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.integration.StructureIntegration;
import dev.jsinco.brewery.bukkit.util.ComponentUtil;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.util.ClassUtil;
import me.angeschossen.lands.api.flags.enums.FlagTarget;
import me.angeschossen.lands.api.flags.enums.RoleFlagCategory;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import me.angeschossen.lands.api.land.LandWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LandsIntegration implements StructureIntegration {

    private static final boolean ENABLED = ClassUtil.exists("me.angeschossen.lands.api.LandsIntegration");
    private static me.angeschossen.lands.api.LandsIntegration landsIntegration;
    private static RoleFlag barrelAccessFlag;
    private static RoleFlag distilleryAccessFlag;
    private static RoleFlag cauldronAccessFlag;

    @Override
    public boolean hasAccess(Block block, Player player, BreweryKey type) {
        if (!ENABLED) {
            return true;
        }
        LandWorld lWorld = landsIntegration.getWorld(block.getWorld());
        if (lWorld == null) {
            return true;
        }
        return switch (type.key()) {
            case "barrel" -> lWorld.hasRoleFlag(player.getUniqueId(), block.getLocation(), barrelAccessFlag);
            case "distillery" -> lWorld.hasRoleFlag(player.getUniqueId(), block.getLocation(), distilleryAccessFlag);
            case "cauldron" -> lWorld.hasRoleFlag(player.getUniqueId(), block.getLocation(), cauldronAccessFlag);
            default -> true;
        };
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
        barrelAccessFlag = registerFlag("barrel_access", Material.BARREL, "barrel-access");
        distilleryAccessFlag = registerFlag("distillery_access", Material.BREWING_STAND, "distillery-access");
        cauldronAccessFlag = registerFlag("cauldron_access", Material.CAULDRON, "cauldron-access");
    }

    private RoleFlag registerFlag(String id, Material icon, String translationKey) {
        MiniMessage mini = MiniMessage.miniMessage();

        String serializedName = mini.serialize(GlobalTranslator.render(
                Component.translatable("tbp.integration.lands.flag." + translationKey + ".name"),
                Config.config().language()
        ));

        Component description = GlobalTranslator.render(
                Component.translatable("tbp.integration.lands.flag." + translationKey + ".description"),
                Config.config().language()
        );
        List<Component> descriptionLines = ComponentUtil.splitIntoLines(description);
        List<String> serializedDescriptionLines = descriptionLines.stream()
                .map(mini::serialize).map(s -> "§r" + s).toList();

        return RoleFlag.of(landsIntegration, FlagTarget.PLAYER, RoleFlagCategory.ACTION, id)
                .setDisplayName(serializedName)
                .setDescription(serializedDescriptionLines)
                .setIcon(new ItemStack(icon))
                .setDisplay(true);
    }

}
