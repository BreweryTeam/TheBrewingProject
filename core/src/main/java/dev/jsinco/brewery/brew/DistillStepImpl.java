package dev.jsinco.brewery.brew;

import java.util.List;

public record DistillStepImpl(int runs) implements BrewingStep.Distill {
    private static final List<PartialBrewScore> BREW_STEP_MISMATCH = List.of(
            new PartialBrewScore(0, PartialBrewScore.Type.DISTILL_AMOUNT)
    );

    @Override
    public DistillStepImpl incrementAmount() {
        return new DistillStepImpl(this.runs + 1);
    }

    @Override
    public List<PartialBrewScore> proximityScores(BrewingStep other) {
        if (!(other instanceof DistillStepImpl(int otherRuns))) {
            return BREW_STEP_MISMATCH;
        }
        double distillScore = Math.sqrt(BrewingStepUtil.nearbyValueScore(this.runs, otherRuns));
        return List.of(new PartialBrewScore(distillScore, PartialBrewScore.Type.DISTILL_AMOUNT));
    }

    @Override
    public StepType stepType() {
        return StepType.DISTILL;
    }

    @Override
    public List<PartialBrewScore> maximumScores(BrewingStep other) {
        if (!(other instanceof DistillStepImpl(int runs1))) {
            return BREW_STEP_MISMATCH;
        }
        double maximumDistillScore = runs1 < this.runs ? 1D : BrewingStepUtil.nearbyValueScore(this.runs, runs1);
        return List.of(new PartialBrewScore(maximumDistillScore, PartialBrewScore.Type.DISTILL_AMOUNT));
    }

    @Override
    public List<PartialBrewScore> failedScores() {
        return BREW_STEP_MISMATCH;
    }
}
