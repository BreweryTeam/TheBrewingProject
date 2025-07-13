package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
@Accessors(fluent = true)
public class BarrelSection {

    @Comment("How many ticks it will take to age a brew one year")
    private long agingYearTicks = Moment.DEFAULT_AGING_YEAR;
}
