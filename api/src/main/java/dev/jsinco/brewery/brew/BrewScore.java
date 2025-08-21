package dev.jsinco.brewery.brew;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BrewScore {

    @Nullable BrewQuality brewQuality();

    List<PartialBrewScore> getPartialScores(int stepIndex) throws IndexOutOfBoundsException;

    double score();

    double rawScore();

    boolean completed();

    String displayName();

    double brewDifficulty();
}
