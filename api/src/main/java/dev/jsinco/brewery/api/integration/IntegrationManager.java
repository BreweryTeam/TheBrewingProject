package dev.jsinco.brewery.api.integration;

import java.util.function.Supplier;

public interface IntegrationManager {

    /**
     * @param type        The integration type
     * @param integration The integration instance
     * @param <T>         An integration type
     */
    <T extends Integration> void register(IntegrationType<? extends T> type, T integration);

    /**
     * Use this one when loading internally. By some reason you can get class loading issues even though this should be dynamic
     *
     * @param type               The integration type
     * @param classNamePredicate A class name to check if it exists
     * @param tSupplier          Constructor for integration
     * @param <T>                The integration type
     */
    <T extends Integration> void register(IntegrationType<? extends T> type, String classNamePredicate, Supplier<T> tSupplier);
}
