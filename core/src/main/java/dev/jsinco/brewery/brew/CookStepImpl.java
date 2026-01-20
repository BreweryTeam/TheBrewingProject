package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.PartialBrewScore;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.api.breweries.CauldronType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.util.CollectionUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record CookStepImpl(Moment time, Map<? extends Ingredient, Integer> ingredients,
                           CauldronType cauldronType, SequencedSet<UUID> brewers) implements BrewingStep.Cook {

    private static final Map<ScoreType, PartialBrewScore> BREW_STEP_MISMATCH = Stream.of(
            new PartialBrewScore(0, ScoreType.TIME),
            new PartialBrewScore(0, ScoreType.INGREDIENTS)
    ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));

    public CookStepImpl(Moment time, Map<? extends Ingredient, Integer> ingredients,
                        CauldronType cauldronType) {
        this(time, ingredients, cauldronType, Collections.emptySortedSet());
    }

    @Override
    public CookStepImpl withBrewTime(Moment brewTime) {
        return new CookStepImpl(brewTime, this.ingredients, this.cauldronType, this.brewers);
    }

    @Override
    public CookStepImpl withIngredients(Map<? extends Ingredient, Integer> ingredients) {
        return new CookStepImpl(this.time, ingredients, this.cauldronType, this.brewers);
    }

    @Override
    public Map<ScoreType, PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof CookStepImpl(
                Moment otherTime, Map<? extends Ingredient, Integer> otherIngredients,
                CauldronType otherType, SequencedSet<UUID> ignored
        ))) {
            return BREW_STEP_MISMATCH;
        }
        double cauldronTypeScore = cauldronType.equals(otherType) ? 1D : 0D;
        double timeScore = Math.sqrt(BrewingStepUtil.nearbyValueScore(this.time.moment(), otherTime.moment()));
        double ingredientsScore = BrewingStepUtil.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients, (Map<Ingredient, Integer>) otherIngredients);
        return Stream.of(
                new PartialBrewScore(cauldronTypeScore * timeScore, ScoreType.TIME),
                new PartialBrewScore(ingredientsScore, ScoreType.INGREDIENTS)
        ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public StepType stepType() {
        return StepType.COOK;
    }

    @Override
    public Map<ScoreType, PartialBrewScore> maximumScores(BrewingStep other) {
        return proximityScores(other);
    }

    @Override
    public Map<ScoreType, PartialBrewScore> failedScores() {
        return BREW_STEP_MISMATCH;
    }

    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public Cook withBrewersReplaced(SequencedCollection<UUID> brewers) {
        return new CookStepImpl(this.time, this.ingredients, this.cauldronType, new LinkedHashSet<>(brewers));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CookStepImpl(
                Moment otherTime, Map<? extends Ingredient, Integer> otherIngredients,
                CauldronType otherType, SequencedSet<UUID> otherBrewers
        ))) {
            return false;
        }
        return Objects.equals(time, otherTime)
                && Objects.equals(ingredients, otherIngredients)
                && cauldronType == otherType
                && CollectionUtil.isEqualWithOrdering(brewers, otherBrewers);
    }
}
