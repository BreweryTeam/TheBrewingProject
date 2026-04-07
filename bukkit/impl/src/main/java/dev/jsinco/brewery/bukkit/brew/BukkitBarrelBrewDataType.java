package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.brew.BarrelBrewDataType;
import dev.jsinco.brewery.bukkit.ingredient.BukkitIngredientManager;
import dev.jsinco.brewery.api.ingredient.IngredientManager;
import org.bukkit.inventory.ItemStack;

public class BukkitBarrelBrewDataType extends BarrelBrewDataType<ItemStack> {
    public static final BukkitBarrelBrewDataType INSTANCE = new BukkitBarrelBrewDataType();

    @Override
    protected IngredientManager<ItemStack> getIngredientManager() {
        return BukkitIngredientManager.INSTANCE;
    }
}
