package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.util.Logging;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class IntegrationRegistry {
    private final EnumMap<IntegrationType, Set<? extends Integration>> integrations = new EnumMap<>(IntegrationType.class);

    public <T extends Integration> void register(IntegrationType type, T integration) {
        Set<? extends Integration> integrationSet = integrations.computeIfAbsent(type, k -> new HashSet<>());

        if (!type.integrationClass.isInstance(integration)) {
            throw new IllegalArgumentException("Cannot register integration, class " + integration.getClass().getName() + " doesn't implement " + type.integrationClass.getName());
        }

        if (integrationSet.stream().anyMatch(existingIntegration -> existingIntegration.getId().equals(integration.getId()))) {
            throw new IllegalStateException("Integration " + type + " already registered");
        }

        @SuppressWarnings("unchecked") // should be safe, T extends Integration, we check anyway
        Set<Integration> rawIntegrationList = (Set<Integration>) integrationSet;

        rawIntegrationList.add(integration);
    }

    @ApiStatus.Internal
    public Set<? extends Integration> getIntegrations(IntegrationType type) {
        Set<? extends Integration> rawSet = integrations.get(type);

        if (rawSet == null) {
            return Collections.emptySet();
        }

        return integrations.get(type);
    }

    @ApiStatus.Internal
    public void clear() {
        integrations.clear();
    }
}
