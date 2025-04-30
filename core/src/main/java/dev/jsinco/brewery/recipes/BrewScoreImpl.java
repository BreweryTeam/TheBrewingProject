package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.brew.BrewQuality;
import dev.jsinco.brewery.brew.BrewScore;
import dev.jsinco.brewery.brew.BrewingStep;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class BrewScoreImpl implements BrewScore {

    public static final BrewScoreImpl EXCELLENT = new BrewScoreImpl(1D);
    public static final BrewScoreImpl NONE = new BrewScoreImpl(0D);
    private static final char FULL_STAR = '\u2605';
    private static final char HALF_STAR = '\u2BEA';
    private static final char EMPTY_STAR = '\u2606';

    private final List<Double> scores;
    private final boolean completed;
    private final double brewDifficulty;

    public @Nullable BrewQuality brewQuality() {
        return quality(score());
    }

    private BrewScoreImpl(double score) {
        double modifiedScore = Math.pow(score, (double) 1 / BrewingStep.StepType.values().length);
        this.scores = Arrays.stream(BrewingStep.StepType.values()).map(ignored -> modifiedScore).toList();
        this.completed = true;
        this.brewDifficulty = 1;
    }

    public BrewScoreImpl(List<Double> scores, boolean completed, double brewDifficulty) {
        this.scores = scores;
        this.completed = completed;
        this.brewDifficulty = brewDifficulty / 2;
    }

    public double getPartialScore(int stepIndex) {
        return applyDifficulty(scores.get(stepIndex));
    }

    public double score() {
        return applyDifficulty(rawScore());
    }

    private double applyDifficulty(double score) {
        score = Math.min(score + 0.05, 1D);
        // Avoid extreme point, log(0) is minus infinity
        if (brewDifficulty <= 0) {
            return 1D;
        }
        double scoreWithDifficulty;
        // Avoid extreme point, can not divide by log(1) as it's 0
        if (brewDifficulty == 1) {
            scoreWithDifficulty = score;
        } else {
            scoreWithDifficulty = (Math.pow(brewDifficulty, 3 * score) - 1) / (Math.pow(brewDifficulty, 3) - 1);
        }
        return Math.max(scoreWithDifficulty - 0.3, 0.0) * 1 / 0.7;
    }

    public String displayName() {
        StringBuilder builder = new StringBuilder();
        int score = (int) (score() * 10);
        int fullStars = score / 2;
        int remainder = score % 2;
        builder.repeat(FULL_STAR, fullStars);
        if (remainder == 1) {
            builder.append(HALF_STAR);
            builder.repeat(EMPTY_STAR, 4 - fullStars);
        } else {
            builder.repeat(EMPTY_STAR, 5 - fullStars);
        }
        return builder.toString();
    }

    public double rawScore() {
        return this.scores.stream()
                .reduce(1D, (aDouble, aDouble2) -> aDouble * aDouble2);
    }

    public boolean completed() {
        return completed;
    }

    public static BrewQuality quality(double score) {
        if (score >= 0.8) {
            return BrewQuality.EXCELLENT;
        }
        if (score >= 0.6) {
            return BrewQuality.GOOD;
        }
        if (score > 0) {
            return BrewQuality.BAD;
        }
        return null;
    }
}
