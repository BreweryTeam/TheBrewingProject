package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.integration.IntegrationManager;
import dev.jsinco.brewery.api.integration.IntegrationType;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.integration.chest_shop.QuickShopHikariIntegration;
import dev.jsinco.brewery.bukkit.integration.event.GSitIntegration;
import dev.jsinco.brewery.bukkit.integration.item.CraftEngineIntegration;
import dev.jsinco.brewery.bukkit.integration.item.ItemsAdderIntegration;
import dev.jsinco.brewery.bukkit.integration.item.MmoItemsIntegration;
import dev.jsinco.brewery.bukkit.integration.item.MythicIntegration;
import dev.jsinco.brewery.bukkit.integration.item.NexoIntegration;
import dev.jsinco.brewery.bukkit.integration.item.OraxenIntegration;
import dev.jsinco.brewery.bukkit.integration.placeholder.MiniPlaceholdersIntegration;
import dev.jsinco.brewery.bukkit.integration.placeholder.PlaceholderApiIntegration;
import dev.jsinco.brewery.bukkit.integration.structure.BoltIntegration;
import dev.jsinco.brewery.bukkit.integration.structure.GriefDefenderIntegration;
import dev.jsinco.brewery.bukkit.integration.structure.GriefPreventionIntegration;
import dev.jsinco.brewery.bukkit.integration.structure.HuskClaimsIntegration;
import dev.jsinco.brewery.bukkit.integration.structure.LandsIntegration;
import dev.jsinco.brewery.bukkit.integration.structure.TownyIntegration;
import dev.jsinco.brewery.bukkit.integration.structure.WorldGuardIntegration;

import java.util.Set;

public class IntegrationManagerImpl implements IntegrationManager {
    private final IntegrationRegistry integrationRegistry = new IntegrationRegistry();

    public void registerIntegrations() {
        register(IntegrationTypes.STRUCTURE, new WorldGuardIntegration());
        register(IntegrationTypes.STRUCTURE, new BoltIntegration());
        register(IntegrationTypes.STRUCTURE, new GriefPreventionIntegration());
        register(IntegrationTypes.STRUCTURE, new HuskClaimsIntegration());
        register(IntegrationTypes.STRUCTURE, new LandsIntegration());
        register(IntegrationTypes.STRUCTURE, new TownyIntegration());
        register(IntegrationTypes.STRUCTURE, new GriefDefenderIntegration());
        register(IntegrationTypes.ITEM, new CraftEngineIntegration());
        register(IntegrationTypes.ITEM, new ItemsAdderIntegration());
        register(IntegrationTypes.ITEM, new NexoIntegration());
        register(IntegrationTypes.ITEM, new OraxenIntegration());
        register(IntegrationTypes.ITEM, new MmoItemsIntegration());
        register(IntegrationTypes.ITEM, new MythicIntegration());
        register(IntegrationTypes.PLACEHOLDER, new PlaceholderApiIntegration());
        register(IntegrationTypes.PLACEHOLDER, new MiniPlaceholdersIntegration());
        register(IntegrationTypes.CHEST_SHOP, new QuickShopHikariIntegration());
        register(IntegrationTypes.EVENT, new GSitIntegration());
    }

    public void loadIntegrations() {
        integrationRegistry.getAllIntegrations()
                .forEach(Integration::onLoad);
    }

    public void enableIntegrations() {
        integrationRegistry.getAllIntegrations()
                .forEach(Integration::onEnable);
    }

    @Override
    public <T extends Integration> void register(IntegrationType<? extends T> type, T integration) {
        if (!integration.isEnabled()) {
            return;
        }

        Logger.log("Registering integration " + integration.getId() + " with type " + type.name());
        integrationRegistry.register(type, integration);
    }

    public void clear() {
        integrationRegistry.clear();
    }

    public <T extends Integration> Set<T> retrieve(IntegrationType<T> type) {
        Set<T> integrations = integrationRegistry.getIntegrations(type);
        if (integrations.isEmpty()) {
            return Set.of();
        }
        return integrations;
    }

    public IntegrationRegistry getIntegrationRegistry() {
        return this.integrationRegistry;
    }
}
