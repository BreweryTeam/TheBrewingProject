package dev.jsinco.brewery.api.brew;

/**
 * @param score The partial score
 * @param type  The type of the partial score
 */
public record PartialBrewScore(double score, ScoreType type) {

}
