package dev.jsinco.brewery.configuration;

import org.spongepowered.configurate.objectmapping.meta.Comment;

public record DecayRateSection(
        @Comment("How many ticks until alcohol level decays by 1%")
        int alcohol,
        @Comment("How many ticks until toxin level decays by 1%")
        int toxin) {

    public static final DecayRateSection DEFAULT = new DecayRateSection(200, 400);
}
