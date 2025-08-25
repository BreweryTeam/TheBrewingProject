package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.moment.Moment;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record CookStepImpl(Moment time, Map<? extends Ingredient, Integer> ingredients,
                           CauldronType cauldronType) implements BrewingStep.Cook {

    private static final Map<PartialBrewScore.Type, PartialBrewScore> BREW_STEP_MISMATCH = Stream.of(
            new PartialBrewScore(0, PartialBrewScore.Type.TIME),
            new PartialBrewScore(0, PartialBrewScore.Type.INGREDIENTS)
    ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));

    @Override
    public CookStepImpl withBrewTime(Moment brewTime) {
        return new CookStepImpl(brewTime, this.ingredients, this.cauldronType);
    }

    @Override
    public CookStepImpl withIngredients(Map<Ingredient, Integer> ingredients) {
        return new CookStepImpl(this.time, ingredients, this.cauldronType);
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof CookStepImpl(
                Moment otherTime, Map<? extends Ingredient, Integer> otherIngredients, CauldronType otherType
        ))) {
            return BREW_STEP_MISMATCH;
        }
        double cauldronTypeScore = cauldronType.equals(otherType) ? 1D : 0D;
        double timeScore = Math.sqrt(BrewingStepUtil.nearbyValueScore(this.time.moment(), otherTime.moment()));
        double ingredientsScore = BrewingStepUtil.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients, (Map<Ingredient, Integer>) otherIngredients);
        return Stream.of(
                new PartialBrewScore(cauldronTypeScore * timeScore, PartialBrewScore.Type.TIME),
                new PartialBrewScore(ingredientsScore, PartialBrewScore.Type.INGREDIENTS)
        ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public StepType stepType() {
        return StepType.COOK;
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> maximumScores(BrewingStep other) {
        if (!(other instanceof Cook cook)) {
            return BREW_STEP_MISMATCH;
        }
        double cauldronTypeScore = cauldronType().equals(cook.cauldronType()) ? 1D : 0D;
        double maximumCookTimeScore = this.time.moment() > cook.time().moment() ? 1D : BrewingStepUtil.nearbyValueScore(this.time.moment(), cook.time().moment());
        double ingredientsScore = BrewingStepUtil.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients(), (Map<Ingredient, Integer>) cook.ingredients());
        return Stream.of(
                new PartialBrewScore(cauldronTypeScore * maximumCookTimeScore, PartialBrewScore.Type.TIME),
                new PartialBrewScore(ingredientsScore, PartialBrewScore.Type.INGREDIENTS)
        ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> failedScores() {
        return BREW_STEP_MISMATCH;
    }
}
