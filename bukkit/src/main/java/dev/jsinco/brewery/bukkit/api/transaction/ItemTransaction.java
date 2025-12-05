package dev.jsinco.brewery.bukkit.api.transaction;

import org.bukkit.inventory.ItemStack;

public record ItemTransaction(InventoryPosition from, InventoryPosition to, ItemStack itemStack, boolean insertion) {


    public ItemStack itemStack() {
        return itemStack.clone();
    }

    public sealed interface InventoryPosition {

    }

    public record RawPosition(int pos) implements InventoryPosition {

    }

    public record Cursor() implements InventoryPosition {

    }
}
