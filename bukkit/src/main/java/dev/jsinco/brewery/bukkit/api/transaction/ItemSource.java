package dev.jsinco.brewery.bukkit.api.transaction;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.bukkit.brew.BrewAdapter;
import org.bukkit.inventory.ItemStack;

public sealed interface ItemSource {

    ItemStack get();

    record ItemBasedSource(ItemStack itemStack) implements ItemSource {

        public ItemStack itemStack() {
            return itemStack.clone();
        }

        @Override
        public ItemStack get() {
            return itemStack.clone();
        }
    }

    record BrewBasedSource(Brew brew, Brew.State state) implements ItemSource {

        @Override
        public ItemStack get() {
            return BrewAdapter.toItem(brew, state);
        }
    }
}
