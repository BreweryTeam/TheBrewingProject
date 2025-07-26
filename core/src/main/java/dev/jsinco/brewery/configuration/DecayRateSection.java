package dev.jsinco.brewery.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class DecayRateSection extends OkaeriConfig {
    @Comment("How many ticks until alcohol level decays by 1%")
    @CustomKey("alcohol")
    private int alcohol = 200;

    @Comment("How many ticks until toxin level decays by 1%")
    @CustomKey("toxin")
    private int toxin = 400;
}
