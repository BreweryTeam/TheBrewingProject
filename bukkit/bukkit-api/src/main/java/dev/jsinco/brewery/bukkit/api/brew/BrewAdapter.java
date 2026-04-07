package dev.jsinco.brewery.bukkit.api.brew;

import dev.jsinco.brewery.api.brew.Brew;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface BrewAdapter {

    ItemStack toItem(Brew brew, Brew.State state);

    Optional<Brew> toBrew(ItemStack itemStack);
}
