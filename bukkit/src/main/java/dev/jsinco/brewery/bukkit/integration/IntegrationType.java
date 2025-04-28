package dev.jsinco.brewery.bukkit.integration;

public enum IntegrationType {
    ITEM(ItemIntegration.class),
    STRUCTURE(StructureIntegration.class);

    public final Class<? extends Integration> integrationClass;

    IntegrationType(Class<? extends Integration> integrationClass) {
        this.integrationClass = integrationClass;
    }
}
