package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.bukkit.integration.structure.*;
import dev.jsinco.brewery.bukkit.integration.item.CraftEngineHook;
import dev.jsinco.brewery.bukkit.integration.item.ItemsAdderHook;
import dev.jsinco.brewery.bukkit.integration.item.NexoHook;
import dev.jsinco.brewery.bukkit.integration.item.OraxenHook;
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
        register(IntegrationType.STRUCTURE, new WorldGuardHook());
        register(IntegrationType.STRUCTURE, new BoltHook());
        register(IntegrationType.STRUCTURE, new GriefPreventionHook());
        register(IntegrationType.STRUCTURE, new HuskClaimsHook());
        register(IntegrationType.STRUCTURE, new LandsHook());
        register(IntegrationType.STRUCTURE, new TownyHook());
        register(IntegrationType.ITEM, new CraftEngineHook());
        register(IntegrationType.ITEM, new ItemsAdderHook());
        register(IntegrationType.ITEM, new NexoHook());
        register(IntegrationType.ITEM, new OraxenHook());
        integrationRegistry.getIntegrations(IntegrationType.ITEM).forEach(Integration::initialize);
        integrationRegistry.getIntegrations(IntegrationType.STRUCTURE).forEach(Integration::initialize);
    }

    public void register(IntegrationType type, Integration integration) {
        if (!integration.enabled()) {
            Logging.log("Skipping registration of integration " + integration.getId() + " with type " + type + " because it is disabled");
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
        Set<StructureIntegration> structureIntegrations = (Set<StructureIntegration>) integrationRegistry.getIntegrations(IntegrationType.STRUCTURE);

        if (structureIntegrations.isEmpty())
            return true;

        return structureIntegrations.stream().allMatch(structureIntegration -> structureIntegration.hasAccess(block, player));
    }
}
