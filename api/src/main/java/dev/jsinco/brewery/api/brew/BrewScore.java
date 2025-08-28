package dev.jsinco.brewery.api.brew;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface BrewScore {

    /**
     * @return The quality of the brew, else null if it's a failed score
     */
    @Nullable BrewQuality brewQuality();

    /**
     * @param stepIndex The step to get the partial scores on
     * @return A map with partial scores
     * @throws IndexOutOfBoundsException If step index is too large, or smaller than 0
     */
    Map<PartialBrewScore.Type, PartialBrewScore> getPartialScores(int stepIndex) throws IndexOutOfBoundsException;

    /**
     * @return A difficulty weighted score
     */
    double score();

    /**
     * @return A raw score, not weighted by difficulty
     */
    double rawScore();

    /**
     * A brew score can also be for uncompleted brews. You can find out whether this is the final step
     * for the recipe related to this score using this method.
     *
     * @return True if there are no more steps left to complete the brew
     */
    boolean completed();

    /**
     * @return A text representation of stars for this score
     */
    String displayName();

    /**
     * @return The difficulty to apply to the weighted score: {@link #score()}
     */
    double brewDifficulty();
}
