package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class BarrelSection extends OkaeriConfig {

    @Comment("How many ticks it will take to age a brew one year")
    @CustomKey("aging-year-ticks")
    private long agingYearTicks = Moment.DEFAULT_AGING_YEAR;
}
