package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.PartialBrewScore;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.util.CollectionUtil;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record DistillStepImpl(int runs, SequencedSet<UUID> brewers, int mergeCount) implements BrewingStep.Distill {

    private static final Map<ScoreType, PartialBrewScore> BREW_STEP_MISMATCH = Stream.of(
            new PartialBrewScore(0, ScoreType.DISTILL_AMOUNT)
    ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));

    public DistillStepImpl(int runs) {
        this(runs, Collections.emptySortedSet(), 1);
    }

    @Override
    public DistillStepImpl incrementRuns() {
        return new DistillStepImpl(this.runs + 1, this.brewers, this.mergeCount);
    }

    @Override
    public Map<ScoreType, PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof DistillStepImpl(int otherRuns, SequencedSet<UUID> ignored, int ignored2))) {
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
        if (!(other instanceof DistillStepImpl(int runs1, SequencedSet<UUID> ignored, int ignored2))) {
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
    public Optional<BrewingStep> merge(BrewingStep otherObject) {
        if (!(otherObject instanceof BrewingStep.Distill other)) {
            return Optional.empty();
        }
        int newRuns = (other.runs() * other.mergeCount() + this.runs * this.mergeCount) / (other.mergeCount() + this.mergeCount);
        SequencedSet<UUID> newBrewers = new LinkedHashSet<>(this.brewers);
        newBrewers.addAll(other.brewers());
        return Optional.of(
                new DistillStepImpl(newRuns, newBrewers, other.mergeCount() + this.mergeCount)
        );
    }

    @Override
    public Distill withBrewersReplaced(SequencedCollection<UUID> brewers) {
        return new DistillStepImpl(this.runs, new LinkedHashSet<>(brewers), this.mergeCount);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DistillStepImpl(int otherRuns, SequencedSet<UUID> otherBrewers, int otherMergeCount))) {
            return false;
        }
        return runs == otherRuns
                && CollectionUtil.isEqualWithOrdering(brewers, otherBrewers)
                && otherMergeCount == this.mergeCount;
    }
}
