package dev.jsinco.brewery.configuration.locale;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record BrewDisplayNameSection(
        String unfinishedAged,
        String unfinishedAgedUnknown,
        String unfinishedDistilled,
        String unfinishedDistilledUnknown,
        String unfinishedFermented,
        String unfinishedFermentedUnknown,
        String unfinishedMixed,
        String unfinishedMixedUnknown
) {

    public static final BrewDisplayNameSection DEFAULT = new BrewDisplayNameSection(
            "unfinished aged",
            "unfinished aged - unknown",
            "unfinished distilled",
            "unfinished distilled - unknown",
            "unfinished cooked",
            "unfinished cooked - unknown",
            "unfinished mixed",
            "unfinished mixed - unknown"
    );
}
