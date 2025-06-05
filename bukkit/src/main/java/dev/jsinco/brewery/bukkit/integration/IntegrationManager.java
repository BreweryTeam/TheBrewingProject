package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.bukkit.integration.item.*;
import dev.jsinco.brewery.bukkit.integration.structure.*;
import dev.jsinco.brewery.util.Logging;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

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
        integrationRegistry.getIntegrations(IntegrationType.ITEM).forEach(Integration::initialize);
        integrationRegistry.getIntegrations(IntegrationType.STRUCTURE).forEach(Integration::initialize);
    }

    public void register(IntegrationType type, Integration integration) {
        if (!integration.enabled()) {
            return;
        }

        Logging.log("Registering integration " + integration.getId() + " with type " + type);
        integrationRegistry.register(type, integration);
    }

    public void clear() {
        integrationRegistry.clear();
    }

    @ApiStatus.Internal
    public boolean hasAccess(Block block, Player player) {
        @SuppressWarnings("unchecked")
        Set<StructureIntegration> structureIntegrations = integrationRegistry.getIntegrations(IntegrationType.STRUCTURE);

        if (structureIntegrations.isEmpty())
            return true;

        return structureIntegrations.stream().allMatch(structureIntegration -> structureIntegration.hasAccess(block, player));
    }
}
