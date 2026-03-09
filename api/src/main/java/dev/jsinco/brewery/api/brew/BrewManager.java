package dev.jsinco.brewery.api.brew;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.meta.MetaData;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BrewManager<I> {

    /**
     * @param steps A list of steps
     * @return A new brew instance with the given steps
     */
    Brew createBrew(List<BrewingStep> steps);

    Brew createBrew(List<BrewingStep> steps, MetaData meta);

    /**
     * @param cookStep A cook step
     * @return A new brew with a cook step as the initial step
     */
    Brew createBrew(BrewingStep.Cook cookStep);

    /**
     * @param mixStep A mix step
     * @return A new brew with a mix step as the initial step
     */
    Brew createBrew(BrewingStep.Mix mixStep);

    /**
     * @param brew  A brew
     * @param state A brew state
     * @return A new item stack with contents according to the brew
     */
    I toItem(Brew brew, Brew.State state);

    /**
     * @param item An item stack
     * @return An optionally present brew if item had brew contents
     */
    Optional<Brew> fromItem(I item);

    /**
     * @param cookingTicks The ticks spent cooking
     * @param ingredients  The added ingredients
     * @param cauldronType The type of cauldron used in the process
     * @return A new cooking step
     */
    BrewingStep.Cook cookingStep(long cookingTicks, Map<? extends Ingredient, Integer> ingredients, CauldronType cauldronType);

    /**
     * @param mixingTicks  The ticks spent mixing
     * @param ingredients  The added ingredients
     * @param cauldronType The type of cauldron used in the process (ignored at the moment)
     * @return A new mixing step
     */
    BrewingStep.Mix mixingStep(long mixingTicks, Map<? extends Ingredient, Integer> ingredients, CauldronType cauldronType);

    /**
     * @param agingTicks The ticks spent aging
     * @param barrelType The type of barrel used in the process
     * @return A new aging step
     */
    BrewingStep.Age agingStep(long agingTicks, BarrelType barrelType);

    /**
     * @param runs The amount of distill runs
     * @return A new distill step
     */
    BrewingStep.Distill distillStep(int runs);
}
