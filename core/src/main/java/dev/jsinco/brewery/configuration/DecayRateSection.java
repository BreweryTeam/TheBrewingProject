package dev.jsinco.brewery.configuration;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
@Accessors(fluent = true)
public class DecayRateSection {
    @Comment("How many ticks until alcohol level decays by 1%")
    private int alcohol = 200;

    @Comment("How many ticks until toxin level decays by 1%")
    private int toxin = 400;
}
