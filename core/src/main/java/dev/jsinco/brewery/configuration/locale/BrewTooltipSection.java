package dev.jsinco.brewery.configuration.locale;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record BrewTooltipSection(
        String age,
        String distill,
        String cook,
        String mix
) {
    public static final BrewTooltipSection DEFAULT = new BrewTooltipSection(
            "age",
            "distill",
            "cook",
            "mix"
    );
}
