package dev.jsinco.brewery.integration;

public record IntegrationType<T extends Integration>(Class<T> integrationClass, String name) {
}
