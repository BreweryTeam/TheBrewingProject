package dev.jsinco.brewery.bukkit.integration;

import dev.jsinco.brewery.integration.IntegrationType;

public class IntegrationTypes {

    public static IntegrationType<ItemIntegration> ITEM = new IntegrationType<>(ItemIntegration.class, "item integration");
    public static IntegrationType<StructureIntegration> STRUCTURE = new IntegrationType<>(StructureIntegration.class, "structure integration");
    public static IntegrationType<PlaceholderIntegration> PLACEHOLDER = new IntegrationType<>(PlaceholderIntegration.class, "placeholder integration");
}
