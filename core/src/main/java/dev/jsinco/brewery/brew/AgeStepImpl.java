package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.moment.Moment;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record AgeStepImpl(Moment time, BarrelType barrelType) implements BrewingStep.Age {

    private static final Map<PartialBrewScore.Type, PartialBrewScore> BREW_STEP_MISMATCH = Stream.of(
                    new PartialBrewScore(0, PartialBrewScore.Type.TIME),
                    new PartialBrewScore(0, PartialBrewScore.Type.BARREL_TYPE)
            )
            .collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));

    public AgeStepImpl withAge(Moment age) {
        return new AgeStepImpl(age, this.barrelType);
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof AgeStepImpl(Moment otherAge, BarrelType otherType))) {
            return BREW_STEP_MISMATCH;
        }
        double barrelTypeScore = barrelType.equals(BarrelType.ANY) || barrelType.equals(otherType) ? 1D : 0.9D;

        return Stream.of(
                new PartialBrewScore(Math.sqrt(BrewingStepUtil.nearbyValueScore(this.time.moment(), otherAge.moment())), PartialBrewScore.Type.TIME),
                new PartialBrewScore(barrelTypeScore, PartialBrewScore.Type.BARREL_TYPE)
        ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public StepType stepType() {
        return StepType.AGE;
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> maximumScores(BrewingStep other) {
        if (!(other instanceof AgeStepImpl(Moment otherAge, BarrelType otherType))) {
            return BREW_STEP_MISMATCH;
        }
        double barrelTypeScore = barrelType.equals(BarrelType.ANY) || barrelType.equals(otherType) ? 1D : 0.9D;
        double timeScore = otherAge.moment() < this.time.moment() ? 1D : BrewingStepUtil.nearbyValueScore(this.time.moment(), otherAge.moment());
        return Stream.of(
                new PartialBrewScore(timeScore, PartialBrewScore.Type.TIME),
                new PartialBrewScore(barrelTypeScore, PartialBrewScore.Type.BARREL_TYPE)
        ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> failedScores() {
        return BREW_STEP_MISMATCH;
    }
}
