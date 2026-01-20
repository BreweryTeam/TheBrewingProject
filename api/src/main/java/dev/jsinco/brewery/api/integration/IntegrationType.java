package dev.jsinco.brewery.api.integration;

public record IntegrationType<T extends Integration>(Class<? super T> integrationClass, String name) {
}
