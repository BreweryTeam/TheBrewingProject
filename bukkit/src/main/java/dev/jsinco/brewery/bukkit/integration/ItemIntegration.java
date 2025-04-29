package dev.jsinco.brewery.bukkit.integration;

import org.bukkit.inventory.ItemStack;

public interface ItemIntegration extends Integration {
    ItemStack createItem(String id);
}
