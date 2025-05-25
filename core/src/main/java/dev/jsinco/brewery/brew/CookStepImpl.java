package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.breweries.CauldronType;
import dev.jsinco.brewery.ingredient.Ingredient;
import dev.jsinco.brewery.moment.Moment;

import java.util.List;
import java.util.Map;

public record CookStepImpl(Moment brewTime, Map<? extends Ingredient, Integer> ingredients,
                           CauldronType cauldronType) implements BrewingStep.Cook {

    private static final List<PartialBrewScore> BREW_STEP_MISMATCH = List.of(
            new PartialBrewScore(0, PartialBrewScore.Type.TIME),
            new PartialBrewScore(0, PartialBrewScore.Type.INGREDIENTS)
    );

    @Override
    public CookStepImpl withBrewTime(Moment brewTime) {
        return new CookStepImpl(brewTime, this.ingredients, this.cauldronType);
    }

    @Override
    public CookStepImpl withIngredients(Map<Ingredient, Integer> ingredients) {
        return new CookStepImpl(this.brewTime, ingredients, this.cauldronType);
    }

    @Override
    public List<PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof CookStepImpl(
                Moment otherTime, Map<? extends Ingredient, Integer> otherIngredients, CauldronType otherType
        ))) {
            return BREW_STEP_MISMATCH;
        }
        double cauldronTypeScore = cauldronType.equals(otherType) ? 1D : 0D;
        double timeScore = Math.sqrt(BrewingStepUtil.nearbyValueScore(this.brewTime.moment(), otherTime.moment()));
        double ingredientsScore = BrewingStepUtil.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients, (Map<Ingredient, Integer>) otherIngredients);
        return List.of(
                new PartialBrewScore(cauldronTypeScore * timeScore, PartialBrewScore.Type.TIME),
                new PartialBrewScore(ingredientsScore, PartialBrewScore.Type.INGREDIENTS)
        );
    }

    @Override
    public StepType stepType() {
        return StepType.COOK;
    }

    @Override
    public List<PartialBrewScore> maximumScores(BrewingStep other) {
        if (!(other instanceof Cook cook)) {
            return BREW_STEP_MISMATCH;
        }
        double cauldronTypeScore = cauldronType().equals(cook.cauldronType()) ? 1D : 0D;
        double maximumCookTimeScore = this.brewTime.moment() > cook.brewTime().moment() ? 1D : BrewingStepUtil.nearbyValueScore(this.brewTime.moment(), cook.brewTime().moment());
        double ingredientsScore = BrewingStepUtil.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients(), (Map<Ingredient, Integer>) cook.ingredients());
        return List.of(
                new PartialBrewScore(cauldronTypeScore * maximumCookTimeScore, PartialBrewScore.Type.TIME),
                new PartialBrewScore(ingredientsScore, PartialBrewScore.Type.INGREDIENTS)
        );
    }

    @Override
    public List<PartialBrewScore> failedScores() {
        return BREW_STEP_MISMATCH;
    }
}
