package dev.jsinco.brewery.bukkit.api.transaction;

import org.bukkit.inventory.ItemStack;

public record ItemTransaction(InventoryPosition from, InventoryPosition to, ItemStack itemStack) {


    public ItemStack itemStack() {
        return itemStack.clone();
    }

    public sealed interface InventoryPosition {

    }

    public record GenericPosition(int pos) implements InventoryPosition {

    }

    public record Cursor() implements InventoryPosition {

    }
}
