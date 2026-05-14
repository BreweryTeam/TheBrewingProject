package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.api.brew.BrewingStep;
import dev.jsinco.brewery.api.brew.PartialBrewScore;
import dev.jsinco.brewery.api.brew.ScoreType;
import dev.jsinco.brewery.api.breweries.BarrelType;
import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.moment.PassedMoment;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.util.CollectionUtil;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.SequencedSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record AgeStepImpl(Moment time, BarrelType barrelType, SequencedSet<UUID> brewers,
                          int mergeCount) implements BrewingStep.Age {

    private static final Map<ScoreType, PartialBrewScore> BREW_STEP_MISMATCH = Stream.of(
                    new PartialBrewScore(0, ScoreType.TIME),
                    new PartialBrewScore(0, ScoreType.BARREL_TYPE)
            )
            .collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));

    public AgeStepImpl(Moment time, BarrelType barrelType) {
        this(time, barrelType, Collections.emptySortedSet(), 1);
    }

    public AgeStepImpl withAge(Moment age) {
        return new AgeStepImpl(age, this.barrelType, this.brewers, this.mergeCount);
    }

    @Override
    public Map<ScoreType, PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof AgeStepImpl(
                Moment otherAge, BarrelType otherType, SequencedSet<UUID> ignored, int ignored2
        ))) {
            return BREW_STEP_MISMATCH;
        }
        double barrelTypeScore = barrelType.proximityScore(otherType);
        return Stream.of(
                new PartialBrewScore(Math.sqrt(BrewingStepUtil.nearbyValueScore(this.time.moment(), otherAge.moment())), ScoreType.TIME),
                new PartialBrewScore(barrelTypeScore, ScoreType.BARREL_TYPE)
        ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public StepType stepType() {
        return StepType.AGE;
    }

    @Override
    public Map<ScoreType, PartialBrewScore> maximumScores(BrewingStep other) {
        if (!(other instanceof AgeStepImpl(
                Moment otherAge, BarrelType otherType, SequencedSet<UUID> ignored, int ignored2
        ))) {
            return BREW_STEP_MISMATCH;
        }
        double barrelTypeScore = barrelType.proximityScore(otherType);
        double timeScore = otherAge.moment() < this.time.moment() ? 1D : BrewingStepUtil.nearbyValueScore(this.time.moment(), otherAge.moment());
        return Stream.of(
                new PartialBrewScore(timeScore, ScoreType.TIME),
                new PartialBrewScore(barrelTypeScore, ScoreType.BARREL_TYPE)
        ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public Map<ScoreType, PartialBrewScore> failedScores() {
        return BREW_STEP_MISMATCH;
    }

    @Override
    public boolean isCompleted() {
        return time().moment() > Config.config().barrels().agingYearTicks() / 2;
    }

    @Override
    public Optional<BrewingStep> merge(BrewingStep otherObject) {
        if (!(otherObject instanceof BrewingStep.Age other)) {
            return Optional.empty();
        }
        if (other.barrelType() != this.barrelType) {
            return Optional.empty();
        }
        long newElapsedTime = (other.time().moment() * other.mergeCount() + this.time().moment() * this.mergeCount) / (other.mergeCount() + this.mergeCount);
        SequencedSet<UUID> newBrewers = new LinkedHashSet<>(this.brewers);
        newBrewers.addAll(other.brewers());
        return Optional.of(
                new AgeStepImpl(new PassedMoment(newElapsedTime), this.barrelType, newBrewers, this.mergeCount + other.mergeCount())
        );
    }

    @Override
    public Age withBrewersReplaced(SequencedCollection<UUID> brewers) {
        return new AgeStepImpl(this.time, this.barrelType, new LinkedHashSet<>(brewers), this.mergeCount);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AgeStepImpl(
                Moment otherTime, BarrelType otherType, SequencedSet<UUID> otherBrewers, int otherMergeCount
        ))) {
            return false;
        }
        return Objects.equals(time, otherTime)
                && barrelType == otherType
                && CollectionUtil.isEqualWithOrdering(brewers, otherBrewers)
                && otherMergeCount == this.mergeCount;
    }
}
