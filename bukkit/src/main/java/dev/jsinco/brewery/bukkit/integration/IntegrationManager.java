package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.bukkit.integration.item.*;
import dev.jsinco.brewery.bukkit.integration.placeholder.MiniPlaceholdersIntegration;
import dev.jsinco.brewery.bukkit.integration.placeholder.PlaceholderApiIntegration;
import dev.jsinco.brewery.bukkit.integration.structure.*;
import dev.jsinco.brewery.util.Logger;
import lombok.Getter;

import java.util.Set;

public class IntegrationManager {
    @Getter
    private final IntegrationRegistry integrationRegistry = new IntegrationRegistry();

    public void init() {
        register(IntegrationType.STRUCTURE, new WorldGuardIntegration());
        register(IntegrationType.STRUCTURE, new BoltIntegration());
        register(IntegrationType.STRUCTURE, new GriefPreventionIntegration());
        register(IntegrationType.STRUCTURE, new HuskClaimsIntegration());
        register(IntegrationType.STRUCTURE, new LandsIntegration());
        register(IntegrationType.STRUCTURE, new TownyIntegration());
        register(IntegrationType.ITEM, new CraftEngineIntegration());
        register(IntegrationType.ITEM, new ItemsAdderIntegration());
        register(IntegrationType.ITEM, new NexoIntegration());
        register(IntegrationType.ITEM, new OraxenIntegration());
        register(IntegrationType.ITEM, new MmoItemsIntegration());
        register(IntegrationType.PLACEHOLDER, new PlaceholderApiIntegration());
        register(IntegrationType.PLACEHOLDER, new MiniPlaceholdersIntegration());
        integrationRegistry.getIntegrations(IntegrationType.ITEM).forEach(Integration::initialize);
        integrationRegistry.getIntegrations(IntegrationType.STRUCTURE).forEach(Integration::initialize);
        integrationRegistry.getIntegrations(IntegrationType.PLACEHOLDER).forEach(Integration::initialize);
    }

    public void register(IntegrationType<?> type, Integration integration) {
        if (!integration.enabled()) {
            return;
        }

        Logger.log("Registering integration " + integration.getId() + " with type " + type);
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
}
