package dev.jsinco.brewery.recipes;

import dev.jsinco.brewery.api.brew.*;
import dev.jsinco.brewery.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BrewScoreImpl implements BrewScore {

    public static final BrewScoreImpl EXCELLENT = new BrewScoreImpl(1D);

    private final List<Map<PartialBrewScore.Type, PartialBrewScore>> scores;
    private final boolean completed;
    private final double brewDifficulty;

    public BrewScoreImpl(double score) {
        this.scores = List.of(Map.of(PartialBrewScore.Type.TIME, new PartialBrewScore(score, PartialBrewScore.Type.TIME)));
        completed = true;
        brewDifficulty = 1;
    }

    public static BrewScoreImpl failed(Brew brew) {
        List<Map<PartialBrewScore.Type, PartialBrewScore>> scores = brew.getCompletedSteps()
                .stream().map(BrewingStep::failedScores)
                .toList();
        return new BrewScoreImpl(scores, true, 1);
    }

    public @Nullable BrewQuality brewQuality() {
        return quality(score());
    }

    public BrewScoreImpl(List<Map<PartialBrewScore.Type, PartialBrewScore>> scores, boolean completed, double brewDifficulty) {
        this.scores = scores;
        this.completed = completed;
        this.brewDifficulty = brewDifficulty / 2;
    }

    @Override
    public Map<PartialBrewScore.Type, PartialBrewScore> getPartialScores(int stepIndex) {
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
    public Component displayName() {
        return Component.translatable(
                "tbp.brew.tooltip.quality-display",
                Argument.tagResolver(MessageUtil.getValueDisplayTagResolver(score() * 100))
        );
    }

    @Override
    public double rawScore() {
        return this.scores.stream()
                .map(Map::values)
                .map(this::rawPartialScore)
                .reduce(1D, (aDouble, aDouble2) -> aDouble * aDouble2);
    }

    private double rawPartialScore(Collection<PartialBrewScore> partialBrewScores) {
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
