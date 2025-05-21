package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.moment.Moment;

import java.util.List;
import java.util.Map;

public record MixStepImpl(Moment time, Map<? extends Ingredient, Integer> ingredients) implements BrewingStep.Mix {

    private static final List<PartialBrewScore> BREW_STEP_MISMATCH = List.of(
            new PartialBrewScore(0, PartialBrewScore.Type.TIME),
            new PartialBrewScore(0, PartialBrewScore.Type.INGREDIENTS)
    );

    @Override
    public MixStepImpl withIngredients(Map<Ingredient, Integer> ingredients) {
        return new MixStepImpl(this.time, ingredients);
    }

    @Override
    public List<PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof MixStepImpl(Moment otherTime, Map<? extends Ingredient, Integer> otherIngredients))) {
            return BREW_STEP_MISMATCH;
        }
        double timeScore = BrewingStepUtil.nearbyValueScore(this.time.moment(), otherTime.moment());
        double ingredientsScore = BrewingStepUtil.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients, (Map<Ingredient, Integer>) otherIngredients);
        return List.of(
                new PartialBrewScore(timeScore, PartialBrewScore.Type.TIME),
                new PartialBrewScore(ingredientsScore, PartialBrewScore.Type.INGREDIENTS)
        );
    }

    @Override
    public StepType stepType() {
        return StepType.MIX;
    }

    @Override
    public List<PartialBrewScore> maximumScores(BrewingStep other) {
        if (!(other instanceof MixStepImpl(Moment time1, Map<? extends Ingredient, Integer> ingredients1))) {
            return BREW_STEP_MISMATCH;
        }
        double mixTimeScore = time1.moment() < this.time.moment() ? 1D : BrewingStepUtil.nearbyValueScore(this.time.moment(), time1.moment());
        double ingredientsScore = BrewingStepUtil.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients(), (Map<Ingredient, Integer>) ingredients1);
        return List.of(
                new PartialBrewScore(mixTimeScore, PartialBrewScore.Type.TIME),
                new PartialBrewScore(ingredientsScore, PartialBrewScore.Type.INGREDIENTS)
        );
    }

    @Override
    public MixStepImpl withTime(Moment time) {
        return new MixStepImpl(time, this.ingredients);
    }
}
