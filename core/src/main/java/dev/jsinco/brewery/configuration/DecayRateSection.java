package dev.jsinco.brewery.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class DecayRateSection {
    @Comment("How many ticks until alcohol level decays by 1%")
    public int alcohol = 200;

    @Comment("How many ticks until toxin level decays by 1%")
    public int toxin = 400;
}
