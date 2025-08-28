package dev.jsinco.brewery.api.brew;

import java.util.Locale;

/**
 * @param score The partial score
 * @param type  The type of the partial score
 */
public record PartialBrewScore(double score, Type type) {

    public enum Type {
        TIME,
        INGREDIENTS,
        DISTILL_AMOUNT,
        BARREL_TYPE;

        public String colorKey() {
            return "quality_color_" + name().toLowerCase(Locale.ROOT);
        }
    }
}
