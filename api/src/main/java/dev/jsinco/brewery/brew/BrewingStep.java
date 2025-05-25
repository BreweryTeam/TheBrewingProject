package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.moment.Moment;

import java.util.List;
import java.util.Map;

public interface BrewingStep {

    /**
     * Calculate how close this brewing step is to another brewing step.
     *
     * @param other The other step to compare to
     * @return A proximity index in the range [0, 1], where 1 is 100% match
     */
    List<PartialBrewScore> proximityScores(BrewingStep other);

    StepType stepType();

    List<PartialBrewScore> maximumScores(BrewingStep other);

    List<PartialBrewScore> failedScores();

    interface Cook extends BrewingStep {
        Moment brewTime();

        Map<? extends Ingredient, Integer> ingredients();

        CauldronType cauldronType();

        Cook withBrewTime(Moment brewTime);

        Cook withIngredients(Map<Ingredient, Integer> ingredients);
    }

    interface Distill extends BrewingStep {
        int runs();

        Distill incrementAmount();
    }

    interface Age extends BrewingStep {

        Moment age();

        BarrelType barrelType();

        Age withAge(Moment age);
    }

    interface Mix extends BrewingStep {

        Moment time();

        Map<? extends Ingredient, Integer> ingredients();

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
