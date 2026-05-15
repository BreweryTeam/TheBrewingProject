package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.recipe.RecipeMatcher;
import dev.jsinco.brewery.bukkit.api.brew.BrewAdapter;
import dev.jsinco.brewery.bukkit.recipe.RecipeMatcherImpl;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class BrewAdapterImpl implements BrewAdapter {
    @Override
    public ItemStack toItem(Brew brew, Brew.State state) {
        return BrewAdapterAccess.toItem(brew, state);
    }

    @Override
    public Optional<Brew> toBrew(ItemStack itemStack) {
        return BrewAdapterAccess.fromItem(itemStack);
    }

    @Override
    public RecipeMatcher.Builder<ItemStack> matcher() {
        return RecipeMatcherImpl.builder();
    }
}
