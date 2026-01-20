package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.PartialBrewScore;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.util.CollectionUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record DistillStepImpl(int runs, SequencedSet<UUID> brewers) implements BrewingStep.Distill {

    private static final Map<ScoreType, PartialBrewScore> BREW_STEP_MISMATCH = Stream.of(
            new PartialBrewScore(0, ScoreType.DISTILL_AMOUNT)
    ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));

    public DistillStepImpl(int runs) {
        this(runs, Collections.emptySortedSet());
    }

    @Override
    public DistillStepImpl incrementRuns() {
        return new DistillStepImpl(this.runs + 1, this.brewers);
    }

    @Override
    public Map<ScoreType, PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof DistillStepImpl(int otherRuns, SequencedSet<UUID> ignored))) {
            return BREW_STEP_MISMATCH;
        }
        double distillScore = Math.sqrt(BrewingStepUtil.nearbyValueScore(this.runs, otherRuns));
        return Stream.of(new PartialBrewScore(distillScore, ScoreType.DISTILL_AMOUNT))
                .collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public StepType stepType() {
        return StepType.DISTILL;
    }

    @Override
    public Map<ScoreType, PartialBrewScore> maximumScores(BrewingStep other) {
        if (!(other instanceof DistillStepImpl(int runs1, SequencedSet<UUID> ignored))) {
            return BREW_STEP_MISMATCH;
        }
        double maximumDistillScore = runs1 < this.runs ? 1D : BrewingStepUtil.nearbyValueScore(this.runs, runs1);
        return Stream.of(new PartialBrewScore(maximumDistillScore, ScoreType.DISTILL_AMOUNT))
                .collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public Map<ScoreType, PartialBrewScore> failedScores() {
        return BREW_STEP_MISMATCH;
    }

    @Override
    public boolean isCompleted() {
        return runs() > 0;
    }

    @Override
    public Distill withBrewersReplaced(SequencedCollection<UUID> brewers) {
        return new DistillStepImpl(this.runs, new LinkedHashSet<>(brewers));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DistillStepImpl(int otherRuns, SequencedSet<UUID> otherBrewers))) {
            return false;
        }
        return runs == otherRuns && CollectionUtil.isEqualWithOrdering(brewers, otherBrewers);
    }
}
