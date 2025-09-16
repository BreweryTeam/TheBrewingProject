package dev.jsinco.brewery.api.brew;

import java.util.Arrays;
import java.util.Locale;

public enum ScoreType {
    TIME("cook-time", "mix-time", "age-years"),
    INGREDIENTS("ingredients"),
    DISTILL_AMOUNT("runs"),
    BARREL_TYPE("barrel-type");

    private String[] aliases;

    ScoreType(String... aliases) {
        this.aliases = aliases;
    }

    public boolean hasAlias(String alias) {
        return Arrays.stream(aliases)
                .anyMatch(alias::equalsIgnoreCase);
    }

    public String colorKey() {
        return "quality_color_" + name().toLowerCase(Locale.ROOT);
    }
}
