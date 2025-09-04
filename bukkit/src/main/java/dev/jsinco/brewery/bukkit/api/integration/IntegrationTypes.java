package dev.jsinco.brewery.bukkit.api.integration;

import dev.jsinco.brewery.api.integration.IntegrationType;

public class IntegrationTypes {

    public static IntegrationType<ItemIntegration> ITEM = new IntegrationType<>(ItemIntegration.class, "item integration");
    public static IntegrationType<StructureIntegration> STRUCTURE = new IntegrationType<>(StructureIntegration.class, "structure integration");
    public static IntegrationType<PlaceholderIntegration> PLACEHOLDER = new IntegrationType<>(PlaceholderIntegration.class, "placeholder integration");
    public static IntegrationType<ChestShopIntegration> CHEST_SHOP = new IntegrationType<>(ChestShopIntegration.class, "placeholder integration");
}
