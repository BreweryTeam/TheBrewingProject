package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.integration.IntegrationType;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class IntegrationRegistry {
    private final Map<IntegrationType<?>, Set<? extends Integration>> integrations = new HashMap<>();

    public <T extends Integration> void register(IntegrationType<? extends T> type, T integration) {
        Set<? extends Integration> integrationSet = integrations.computeIfAbsent(type, k -> new HashSet<>());

        if (!type.integrationClass().isInstance(integration)) {
            throw new IllegalArgumentException("Cannot register integration, class " + integration.getClass().getName() + " doesn't implement " + type.integrationClass().getName());
        }

        if (integrationSet.stream().anyMatch(existingIntegration -> existingIntegration.getId().equals(integration.getId()))) {
            throw new IllegalStateException("Integration " + type + " already registered");
        }

        @SuppressWarnings("unchecked") // should be safe, T extends Integration, we check anyway
        Set<Integration> rawIntegrationList = (Set<Integration>) integrationSet;

        rawIntegrationList.add(integration);
    }

    @ApiStatus.Internal
    public <T extends Integration> Set<T> getIntegrations(IntegrationType<T> type) {
        Set<? extends Integration> rawSet = integrations.get(type);

        if (rawSet == null) {
            return Collections.emptySet();
        }

        return (Set<T>) integrations.get(type);
    }

    @ApiStatus.Internal
    public void clear() {
        integrations.clear();
    }
}
