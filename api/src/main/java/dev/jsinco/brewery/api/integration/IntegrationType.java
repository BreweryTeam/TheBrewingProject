package dev.jsinco.brewery.api.integration;

public record IntegrationType<T extends Integration>(Class<T> integrationClass, String name) {
}
