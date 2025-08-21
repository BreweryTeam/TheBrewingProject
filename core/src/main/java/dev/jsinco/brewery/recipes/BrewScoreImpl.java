package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.brew.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrewScoreImpl implements BrewScore {

    public static final BrewScoreImpl EXCELLENT = new BrewScoreImpl(1D);
    private static final char FULL_STAR = '\u2605';
    private static final char HALF_STAR = '\u2BEA';
    private static final char EMPTY_STAR = '\u2606';

    private final List<List<PartialBrewScore>> scores;
    private final boolean completed;
    private final double brewDifficulty;

    public BrewScoreImpl(double score) {
        this.scores = List.of(List.of(new PartialBrewScore(score, PartialBrewScore.Type.TIME)));
        completed = true;
        brewDifficulty = 1;
    }

    public static BrewScoreImpl failed(Brew brew) {
        List<List<PartialBrewScore>> scores = brew.getCompletedSteps()
                .stream().map(BrewingStep::failedScores)
                .toList();
        return new BrewScoreImpl(scores, true, 1);
    }

    public @Nullable BrewQuality brewQuality() {
        return quality(score());
    }

    public BrewScoreImpl(List<List<PartialBrewScore>> scores, boolean completed, double brewDifficulty) {
        this.scores = scores;
        this.completed = completed;
        this.brewDifficulty = brewDifficulty / 2;
    }

    @Override
    public List<PartialBrewScore> getPartialScores(int stepIndex) {
        return scores.get(stepIndex);
    }

    @Override
    public double score() {
        return applyDifficulty(rawScore(), brewDifficulty);
    }

    public static double applyDifficulty(double score, double brewDifficulty) {
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

    @Override
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

    @Override
    public double rawScore() {
        return this.scores.stream()
                .map(this::rawPartialScore)
                .reduce(1D, (aDouble, aDouble2) -> aDouble * aDouble2);
    }

    private double rawPartialScore(List<PartialBrewScore> partialBrewScores) {
        return partialBrewScores.stream()
                .map(PartialBrewScore::score)
                .map(score -> partialBrewScores.size() == 1 ? score : Math.sqrt(score))
                .reduce(1D, (value1, value2) -> value1 * value2);
    }

    @Override
    public boolean completed() {
        return completed;
    }

    @Override
    public double brewDifficulty() {
        return brewDifficulty;
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
