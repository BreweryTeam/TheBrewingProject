package dev.jsinco.brewery.brew;

import java.util.Locale;

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
