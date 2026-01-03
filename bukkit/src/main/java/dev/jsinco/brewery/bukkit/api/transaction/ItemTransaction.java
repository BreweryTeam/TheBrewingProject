package dev.jsinco.brewery.bukkit.api.transaction;

import org.bukkit.inventory.ItemStack;

public record ItemTransaction(InventoryPosition from, InventoryPosition to, ItemStack itemStack, boolean insertion) {

    public ItemTransaction(InventoryPosition from, InventoryPosition to, ItemStack itemStack, boolean insertion) {
        this.from = from;
        this.to = to;
        this.itemStack = itemStack.clone();
        this.insertion = insertion;
    }

    public ItemStack itemStack() {
        return itemStack.clone();
    }

    public sealed interface InventoryPosition {

    }

    public record RawPosition(int pos) implements InventoryPosition {

    }

    public record Cursor() implements InventoryPosition {

    }

    public record LowerInventoryPosition(int pos) implements InventoryPosition {

    }

    public record UpperInventoryPosition(int pos) implements InventoryPosition {

    }

    public record FirstInventoryPosition(boolean breweryInventory) implements InventoryPosition {

    }
}
