package dev.jsinco.brewery.bukkit.integration;

public record IntegrationType<T extends Integration>(Class<T> integrationClass) {
    public static IntegrationType<ItemIntegration> ITEM = new IntegrationType<>(ItemIntegration.class);
    public static IntegrationType<StructureIntegration> STRUCTURE = new IntegrationType<>(StructureIntegration.class);
    public static IntegrationType<PlaceholderIntegration> PLACEHOLDER = new IntegrationType<>(PlaceholderIntegration.class);
}
