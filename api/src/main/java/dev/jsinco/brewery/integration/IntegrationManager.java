package dev.jsinco.brewery.integration;

public interface IntegrationManager {

    /**
     * @param type        The integration type
     * @param integration The integration instance
     * @param <T>         An integration type
     */
    <T extends Integration> void register(IntegrationType<? extends T> type, T integration);
}
