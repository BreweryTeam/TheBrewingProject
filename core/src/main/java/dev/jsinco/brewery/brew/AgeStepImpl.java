package dev.jsinco.brewery.brew;

import dev.jsinco.brewery.breweries.BarrelType;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.MessageUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;

public record AgeStepImpl(Moment time, BarrelType barrelType) implements BrewingStep.Age {

    private static final List<PartialBrewScore> BREW_STEP_MISMATCH = List.of(
            new PartialBrewScore(0, PartialBrewScore.Type.TIME),
            new PartialBrewScore(0, PartialBrewScore.Type.BARREL_TYPE)
    );

    public AgeStepImpl withAge(Moment age) {
        return new AgeStepImpl(age, this.barrelType);
    }

    @Override
    public List<PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof AgeStepImpl(Moment otherAge, BarrelType otherType))) {
            return BREW_STEP_MISMATCH;
        }
        double barrelTypeScore = barrelType.equals(BarrelType.ANY) || barrelType.equals(otherType) ? 1D : 0.9D;

        return List.of(
                new PartialBrewScore(Math.sqrt(BrewingStepUtil.nearbyValueScore(this.time.moment(), otherAge.moment())), PartialBrewScore.Type.TIME),
                new PartialBrewScore(barrelTypeScore, PartialBrewScore.Type.BARREL_TYPE)
        );
    }

    @Override
    public StepType stepType() {
        return StepType.AGE;
    }

    @Override
    public List<PartialBrewScore> maximumScores(BrewingStep other) {
        if (!(other instanceof AgeStepImpl(Moment otherAge, BarrelType otherType))) {
            return BREW_STEP_MISMATCH;
        }
        double barrelTypeScore = barrelType.equals(BarrelType.ANY) || barrelType.equals(otherType) ? 1D : 0.9D;
        double timeScore = otherAge.moment() < this.time.moment() ? 1D : BrewingStepUtil.nearbyValueScore(this.time.moment(), otherAge.moment());
        return List.of(
                new PartialBrewScore(timeScore, PartialBrewScore.Type.TIME),
                new PartialBrewScore(barrelTypeScore, PartialBrewScore.Type.BARREL_TYPE)
        );
    }

    @Override
    public List<PartialBrewScore> failedScores() {
        return BREW_STEP_MISMATCH;
    }
}
