package dev.jsinco.brewery.brew;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface BrewScore {

    @Nullable BrewQuality brewQuality();

    Map<PartialBrewScore.Type, PartialBrewScore> getPartialScores(int stepIndex) throws IndexOutOfBoundsException;

    double score();

    double rawScore();

    boolean completed();

    String displayName();

    double brewDifficulty();
}
