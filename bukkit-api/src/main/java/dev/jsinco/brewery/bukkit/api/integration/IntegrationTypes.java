package dev.jsinco.brewery.bukkit.api.integration;

import dev.jsinco.brewery.api.integration.IntegrationType;

public class IntegrationTypes {

    public static IntegrationType<ItemIntegration> ITEM = new IntegrationType<>(ItemIntegration.class, "item");
    public static IntegrationType<StructureIntegration> STRUCTURE = new IntegrationType<>(StructureIntegration.class, "structure");
    public static IntegrationType<PlaceholderIntegration> PLACEHOLDER = new IntegrationType<>(PlaceholderIntegration.class, "placeholder");
    public static IntegrationType<ChestShopIntegration> CHEST_SHOP = new IntegrationType<>(ChestShopIntegration.class, "chest_shop");
    public static IntegrationType<EventIntegration<?>> EVENT = new IntegrationType<>(EventIntegration.class, "event");
}
