package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.moment.Moment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.Argument;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface BrewingStep {

    /**
     * Calculate how close this brewing step is to another brewing step.
     *
     * @param other The other step to compare to
     * @return A proximity index in the range [0, 1], where 1 is 100% match
     */
    List<PartialBrewScore> proximityScores(BrewingStep other);

    /**
     * @return The type of the step
     */
    StepType stepType();

    /**
     * @param other Another brewing step
     * @return The maximum scores "other" can get for this brewing step
     */
    List<PartialBrewScore> maximumScores(BrewingStep other);

    List<PartialBrewScore> failedScores();

    default Component infoDisplay(Brew.State state, TagResolver resolver) {
        return Component.translatable(switch (state) {
            case Brew.State.Other ignored -> "tbp.brew.tooltip." + stepType().name().toLowerCase(Locale.ROOT);
            case Brew.State.Seal ignored -> "tbp.brew.tooltip-sealed." + stepType().name().toLowerCase(Locale.ROOT);
            case Brew.State.Brewing ignored -> "tbp.brew.tooltip-brewing." + stepType().name().toLowerCase(Locale.ROOT);
        }, Argument.tagResolver(resolver));
    }

    interface TimedStep {
        Moment time();
    }

    interface IngredientsStep {
        Map<? extends Ingredient, Integer> ingredients();
    }

    interface Cook extends BrewingStep, TimedStep, IngredientsStep {

        CauldronType cauldronType();

        Cook withBrewTime(Moment brewTime);

        Cook withIngredients(Map<Ingredient, Integer> ingredients);
    }

    interface Distill extends BrewingStep {
        int runs();

        Distill incrementAmount();
    }

    interface Age extends BrewingStep, TimedStep {

        BarrelType barrelType();

        Age withAge(Moment age);
    }

    interface Mix extends BrewingStep, TimedStep, IngredientsStep {

        Mix withIngredients(Map<Ingredient, Integer> ingredients);

        Mix withTime(Moment time);
    }

    enum StepType {
        COOK,
        DISTILL,
        AGE,
        MIX
    }

}
