package dev.jsinco.brewery.brew;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record DistillStepImpl(int runs) implements BrewingStep.Distill {
    private static final Map<PartialBrewScore.Type, PartialBrewScore> BREW_STEP_MISMATCH = Stream.of(
            new PartialBrewScore(0, PartialBrewScore.Type.DISTILL_AMOUNT)
    ).collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));

    @Override
    public DistillStepImpl incrementAmount() {
        return new DistillStepImpl(this.runs + 1);
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof DistillStepImpl(int otherRuns))) {
            return BREW_STEP_MISMATCH;
        }
        double distillScore = Math.sqrt(BrewingStepUtil.nearbyValueScore(this.runs, otherRuns));
        return Stream.of(new PartialBrewScore(distillScore, PartialBrewScore.Type.DISTILL_AMOUNT))
                .collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public StepType stepType() {
        return StepType.DISTILL;
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> maximumScores(BrewingStep other) {
        if (!(other instanceof DistillStepImpl(int runs1))) {
            return BREW_STEP_MISMATCH;
        }
        double maximumDistillScore = runs1 < this.runs ? 1D : BrewingStepUtil.nearbyValueScore(this.runs, runs1);
        return Stream.of(new PartialBrewScore(maximumDistillScore, PartialBrewScore.Type.DISTILL_AMOUNT))
                .collect(Collectors.toUnmodifiableMap(PartialBrewScore::type, partial -> partial));
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> failedScores() {
        return BREW_STEP_MISMATCH;
    }
}
