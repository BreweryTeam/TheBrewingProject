package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.PartialBrewScore;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.util.CollectionUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record MixStepImpl(Moment time, Map<? extends Ingredient, Integer> ingredients,
                          SequencedSet<UUID> brewers) implements BrewingStep.Mix {

    private static final Map<ScoreType, PartialBrewScore> BREW_STEP_MISMATCH = Stream.of(
            new PartialBrewScore(0, ScoreType.TIME),
            new PartialBrewScore(0, ScoreType.INGREDIENTS)
    ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));

    public MixStepImpl(Moment time, Map<? extends Ingredient, Integer> ingredients) {
        this(time, ingredients, Collections.emptySortedSet());
    }

    @Override
    public MixStepImpl withIngredients(Map<? extends Ingredient, Integer> ingredients) {
        return new MixStepImpl(this.time, ingredients, this.brewers);
    }

    @Override
    public Map<ScoreType, PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof MixStepImpl(
                Moment otherTime, Map<? extends Ingredient, Integer> otherIngredients, SequencedSet<UUID> ignored
        ))) {
            return BREW_STEP_MISMATCH;
        }
        double timeScore = BrewingStepUtil.nearbyValueScore(this.time.moment(), otherTime.moment());
        double ingredientsScore = BrewingStepUtil.getIngredientsScore((Map<Ingredient, Integer>) this.ingredients, (Map<Ingredient, Integer>) otherIngredients);
        return Stream.of(
                new PartialBrewScore(timeScore, ScoreType.TIME),
                new PartialBrewScore(ingredientsScore, ScoreType.INGREDIENTS)
        ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public StepType stepType() {
        return StepType.MIX;
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
    public MixStepImpl withTime(Moment time) {
        return new MixStepImpl(time, this.ingredients, this.brewers);
    }

    @Override
    public Mix withBrewersReplaced(SequencedCollection<UUID> brewers) {
        return new MixStepImpl(this.time, this.ingredients, new LinkedHashSet<>(brewers));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MixStepImpl(
                Moment otherTime, Map<? extends Ingredient, Integer> otherIngredients, SequencedSet<UUID> otherBrewers
        ))) {
            return false;
        }
        return Objects.equals(time, otherTime)
                && Objects.equals(ingredients, otherIngredients)
                && CollectionUtil.isEqualWithOrdering(brewers, otherBrewers);
    }
}
