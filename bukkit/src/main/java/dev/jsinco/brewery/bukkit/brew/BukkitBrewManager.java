package dev.jsinco.brewery.bukkit.brew;

import dev.jsinco.brewery.api.brew.Brew;
import dev.jsinco.brewery.api.brew.BrewManager;
import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.meta.MetaData;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.brew.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BukkitBrewManager implements BrewManager<ItemStack> {
    @Override
    public Brew createBrew(List<BrewingStep> steps) {
        return new BrewImpl(steps);
    }

    @Override
    public Brew createBrew(List<BrewingStep> steps, MetaData meta) {
        return new BrewImpl(steps, meta);
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

    @Override
    public BrewingStep.Cook cookingStep(long cookingTicks, Map<? extends Ingredient, Integer> ingredients, CauldronType cauldronType) {
        return new CookStepImpl(
                new PassedMoment(cookingTicks),
                ingredients,
                cauldronType
        );
    }

    @Override
    public BrewingStep.Mix mixingStep(long mixingTicks, Map<? extends Ingredient, Integer> ingredients, CauldronType cauldronType) {
        return new MixStepImpl(
                new PassedMoment(mixingTicks),
                ingredients
        );
    }

    @Override
    public BrewingStep.Age agingStep(long agingTicks, BarrelType barrelType) {
        return new AgeStepImpl(
                new PassedMoment(agingTicks),
                barrelType
        );
    }

    @Override
    public BrewingStep.Distill distillStep(int runs) {
        return new DistillStepImpl(runs);
    }
}
