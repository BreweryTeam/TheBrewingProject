package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.PartialBrewScore;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.moment.Moment;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MixStepImpl(Moment time, Map<? extends Ingredient, Integer> ingredients) implements BrewingStep.Mix {

    private static final Map<PartialBrewScore.Type, PartialBrewScore> BREW_STEP_MISMATCH = Stream.of(
            new PartialBrewScore(0, PartialBrewScore.Type.TIME),
            new PartialBrewScore(0, PartialBrewScore.Type.INGREDIENTS)
    ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));

    @Override
    public MixStepImpl withIngredients(Map<Ingredient, Integer> ingredients) {
        return new MixStepImpl(this.time, ingredients);
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof MixStepImpl(Moment otherTime, Map<? extends Ingredient, Integer> otherIngredients))) {
            return BREW_STEP_MISMATCH;
        }
        double timeScore = BrewingStepUtil.nearbyValueScore(this.time.moment(), otherTime.moment());
        double ingredientsScore = BrewingStepUtil.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients, (Map<Ingredient, Integer>) otherIngredients);
        return Stream.of(
                new PartialBrewScore(timeScore, PartialBrewScore.Type.TIME),
                new PartialBrewScore(ingredientsScore, PartialBrewScore.Type.INGREDIENTS)
        ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public StepType stepType() {
        return StepType.MIX;
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> maximumScores(BrewingStep other) {
        if (!(other instanceof MixStepImpl(Moment time1, Map<? extends Ingredient, Integer> ingredients1))) {
            return BREW_STEP_MISMATCH;
        }
        double mixTimeScore = time1.moment() < this.time.moment() ? 1D : BrewingStepUtil.nearbyValueScore(this.time.moment(), time1.moment());
        double ingredientsScore = BrewingStepUtil.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients(), (Map<Ingredient, Integer>) ingredients1);
        return Stream.of(
                new PartialBrewScore(mixTimeScore, PartialBrewScore.Type.TIME),
                new PartialBrewScore(ingredientsScore, PartialBrewScore.Type.INGREDIENTS)
        ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> failedScores() {
        return BREW_STEP_MISMATCH;
    }

    @Override
    public MixStepImpl withTime(Moment time) {
        return new MixStepImpl(time, this.ingredients);
    }
}
