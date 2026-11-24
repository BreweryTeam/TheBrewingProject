package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.api.integration.Integration;
import dev.jsinco.brewery.api.integration.IntegrationManager;
import dev.jsinco.brewery.api.integration.IntegrationType;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.api.util.ClassUtil;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class IntegrationManagerImpl implements IntegrationManager {
    private final IntegrationRegistry integrationRegistry = new IntegrationRegistry();

    public void loadIntegrations() {
        integrationRegistry.getAllIntegrations()
                .forEach(integration -> {
                    try {
                        integration.onLoad();
                    } catch (Throwable e) {
                        Logger.logErr("Failed loading integration: %s".formatted(integration.getId()));
                        Logger.logAndTrackErr(e);
                    }
                });
    }

    public void enableIntegrations(boolean serverAlreadyRunning) {
        integrationRegistry.getAllIntegrations()
                .forEach(integration -> {
                    try {
                        integration.onEnable();
                        if (serverAlreadyRunning) {
                            integration.onHotReload();
                        }
                    } catch (Throwable e) {
                        Logger.logErr("Failed loading integration: %s".formatted(integration.getId()));
                        Logger.logAndTrackErr(e);
                    }
                });
    }

    @Override
    public <T extends Integration> void register(IntegrationType<? extends T> type, T integration) {
        String fullId = (type.name() + "." + integration.getId()).toLowerCase(Locale.ROOT);
        if (!integration.isEnabled() || Config.config().integrationBlacklist()
                .stream()
                .filter(Objects::nonNull)
                .map(string -> string.toLowerCase(Locale.ROOT))
                .anyMatch(fullId::contains)) {
            return;
        }

        Logger.log("Registering integration " + integration.getId() + " with type " + type.name() + " integration");
        integrationRegistry.register(type, integration);
    }

    /**
     * Use this one when loading internally. By some reason you can get class loading issues even though this should be dynamic
     *
     * @param type               The integration type
     * @param classNamePredicate A class name to check if it exists
     * @param tSupplier          Constructor for integration
     * @param <T>                The integration type
     */
    public <T extends Integration> void register(IntegrationType<? extends T> type, String classNamePredicate, Supplier<T> tSupplier) {
        if (!ClassUtil.exists(classNamePredicate)) {
            return;
        }
        register(type, tSupplier.get());
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
