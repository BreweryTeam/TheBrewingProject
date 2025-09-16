package dev.jsinco.brewery.api.brew;

import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.moment.Moment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.translation.Argument;

import java.util.Locale;
import java.util.Map;

public interface BrewingStep {

    /**
     * Calculate how close this brewing step is to another brewing step.
     *
     * @param other The other step to compare to
     * @return A proximity index in the range [0, 1], where 1 is 100% match
     */
    Map<ScoreType, PartialBrewScore> proximityScores(BrewingStep other);

    /**
     * @return The type of the step
     */
    StepType stepType();

    /**
     * @param other Another brewing step
     * @return The maximum scores "other" can get for this brewing step
     */
    Map<ScoreType, PartialBrewScore> maximumScores(BrewingStep other);

    /**
     * @return The scores for a failed step of this type
     */
    Map<ScoreType, PartialBrewScore> failedScores();

    /**
     * @param state    The state of the brew
     * @param resolver A tag resolver for this step
     * @return A translatable component for displaying this step
     */
    default Component infoDisplay(Brew.State state, TagResolver resolver) {
        return Component.translatable(switch (state) {
            case Brew.State.Other ignored -> "tbp.brew.tooltip." + stepType().name().toLowerCase(Locale.ROOT);
            case Brew.State.Seal ignored -> "tbp.brew.tooltip-sealed." + stepType().name().toLowerCase(Locale.ROOT);
            case Brew.State.Brewing ignored -> "tbp.brew.tooltip-brewing." + stepType().name().toLowerCase(Locale.ROOT);
        }, Argument.tagResolver(resolver));
    }

    interface TimedStep {
        /**
         * @return The time for this step (ticks)
         */
        Moment time();
    }

    interface IngredientsStep {
        /**
         * @return The ingredients for this step
         */
        Map<? extends Ingredient, Integer> ingredients();
    }

    interface Cook extends BrewingStep, TimedStep, IngredientsStep {

        /**
         * @return The type of the cauldron
         */
        CauldronType cauldronType();

        /**
         * @param brewTime A brew time (ticks)
         * @return A new instance of this step with specified brew time
         */
        Cook withBrewTime(Moment brewTime);

        /**
         * @param ingredients A map of ingredients with amount
         * @return A new instance of this step with the specified ingredients
         */
        Cook withIngredients(Map<Ingredient, Integer> ingredients);
    }

    interface Distill extends BrewingStep {

        /**
         * @return The amount of distill runs for this step
         */
        int runs();

        /**
         * @return A new instance of this step with distill runs incremented by 1
         */
        Distill incrementRuns();
    }

    interface Age extends BrewingStep, TimedStep {

        /**
         * @return The type of the barrel
         */
        BarrelType barrelType();

        /**
         * @param age An aging time (ticks)
         * @return A new instance of this step with specified aging time
         */
        Age withAge(Moment age);
    }

    interface Mix extends BrewingStep, TimedStep, IngredientsStep {

        /**
         * @param ingredients A map of ingredients with amount
         * @return A new instance of this step with specified ingredients
         */
        Mix withIngredients(Map<Ingredient, Integer> ingredients);

        /**
         * @param time A time (ticks)
         * @return A new instance of this step with specified time
         */
        Mix withTime(Moment time);
    }

    enum StepType {
        COOK,
        DISTILL,
        AGE,
        MIX
    }

}
