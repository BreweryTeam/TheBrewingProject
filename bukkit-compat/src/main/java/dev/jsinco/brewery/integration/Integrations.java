package dev.jsinco.brewery.integration;

import dev.jsinco.brewery.api.integration.IntegrationManager;
import dev.jsinco.brewery.api.util.color.ResourcePackColors;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.integration.hook.auraskills.AuraSkillsIntegration;
import dev.jsinco.brewery.integration.chest_shop.QuickShopHikariIntegration;
import dev.jsinco.brewery.integration.event.BodyHealthIntegration;
import dev.jsinco.brewery.integration.event.GSitIntegration;
import dev.jsinco.brewery.integration.item.CraftEngineIntegration;
import dev.jsinco.brewery.integration.item.ItemsAdderIntegration;
import dev.jsinco.brewery.integration.item.MmoItemsIntegration;
import dev.jsinco.brewery.integration.item.MythicIntegration;
import dev.jsinco.brewery.integration.item.NexoIntegration;
import dev.jsinco.brewery.integration.item.OraxenIntegration;
import dev.jsinco.brewery.integration.placeholder.MiniPlaceholdersIntegration;
import dev.jsinco.brewery.integration.placeholder.PlaceholderApiIntegration;
import dev.jsinco.brewery.integration.structure.BoltIntegration;
import dev.jsinco.brewery.integration.structure.GriefDefenderIntegration;
import dev.jsinco.brewery.integration.structure.GriefPreventionIntegration;
import dev.jsinco.brewery.integration.structure.HuskClaimsIntegration;
import dev.jsinco.brewery.integration.structure.LandsIntegration;
import dev.jsinco.brewery.integration.structure.TownyIntegration;
import dev.jsinco.brewery.integration.structure.WorldGuardIntegration;
import org.bukkit.plugin.Plugin;

public final class Integrations {

    private Integrations() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void registerIntegrations(ResourcePackColors resourcePackColors, TheBrewingProjectApi api, Plugin owner) {
        IntegrationManager manager = api.getIntegrationManager();
        /*
        Don't replace these with a method reference. Class loading issues otherwise
         */
        manager.register(IntegrationTypes.STRUCTURE, "com.sk89q.worldguard.WorldGuard", () -> new WorldGuardIntegration());
        manager.register(IntegrationTypes.STRUCTURE, "org.popcraft.bolt.BoltAPI", () -> new BoltIntegration(api));
        manager.register(IntegrationTypes.STRUCTURE, "me.ryanhamshire.GriefPrevention.GriefPrevention", () -> new GriefPreventionIntegration());
        manager.register(IntegrationTypes.STRUCTURE, "net.william278.huskclaims.api.BukkitHuskClaimsAPI", () -> new HuskClaimsIntegration());
        manager.register(IntegrationTypes.STRUCTURE, "me.angeschossen.lands.api.LandsIntegration", () -> new LandsIntegration(api, owner));
        manager.register(IntegrationTypes.STRUCTURE, "com.palmergames.bukkit.towny.utils.PlayerCacheUtil", () -> new TownyIntegration());
        manager.register(IntegrationTypes.STRUCTURE, "com.griefdefender.api.GriefDefender", () -> new GriefDefenderIntegration());
        manager.register(IntegrationTypes.ITEM, "net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine", () -> new CraftEngineIntegration(resourcePackColors, owner));
        manager.register(IntegrationTypes.ITEM, "dev.lone.itemsadder.api.CustomStack", () -> new ItemsAdderIntegration(resourcePackColors, owner));
        manager.register(IntegrationTypes.ITEM, "com.nexomc.nexo.api.NexoItems", () -> new NexoIntegration(resourcePackColors, owner));
        manager.register(IntegrationTypes.ITEM, "io.th0rgal.oraxen.api.OraxenItems", () -> new OraxenIntegration(resourcePackColors, owner));
        manager.register(IntegrationTypes.ITEM, "net.Indyuce.mmoitems.MMOItems", () -> new MmoItemsIntegration(resourcePackColors, owner));
        manager.register(IntegrationTypes.ITEM, "io.lumine.mythic.bukkit.MythicBukkit", () -> new MythicIntegration(resourcePackColors, owner));
        manager.register(IntegrationTypes.PLACEHOLDER, "me.clip.placeholderapi.expansion.PlaceholderExpansion", () -> new PlaceholderApiIntegration(api, owner));
        manager.register(IntegrationTypes.PLACEHOLDER, "io.github.miniplaceholders.api.utils.TagsUtils", () -> new MiniPlaceholdersIntegration(api));
        manager.register(IntegrationTypes.CHEST_SHOP, "com.ghostchu.quickshop.api.event.general.ShopItemMatchEvent", () -> new QuickShopHikariIntegration(api, owner));
        manager.register(IntegrationTypes.EVENT, "dev.geco.gsit.api.GSitAPI", () -> new GSitIntegration(owner));
        manager.register(IntegrationTypes.EVENT, "bodyhealth.api.BodyHealthAPI", () -> new BodyHealthIntegration());
        manager.register(IntegrationTypes.GENERIC, "dev.aurelium.auraskills.api.AuraSkillsApi", () -> new AuraSkillsIntegration(owner, api));
    }
}
