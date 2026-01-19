package dev.jsinco.brewery.api.integration;

public record IntegrationType<T extends Integration>(Class<? extends T> integrationClass, String name) {
}
