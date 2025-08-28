package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.brew.BrewImpl;
import dev.jsinco.brewery.api.brew.BrewManager;
import dev.jsinco.brewery.api.brew.BrewingStep;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class BukkitBrewManager implements BrewManager<ItemStack> {
    @Override
    public Brew createBrew(List<BrewingStep> steps) {
        return new BrewImpl(steps);
    }

    @Override
    public Brew createBrew(BrewingStep.Cook cookStep) {
        return new BrewImpl(cookStep);
    }

    @Override
    public Brew createBrew(BrewingStep.Mix mixStep) {
        return new BrewImpl(mixStep);
    }

    @Override
    public ItemStack toItem(Brew brew, Brew.State brewState) {
        return BrewAdapter.toItem(brew, brewState);
    }

    @Override
    public Optional<Brew> fromItem(ItemStack item) {
        return BrewAdapter.fromItem(item);
    }
}
