package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public record BarrelSection(
        @Comment("How many ticks it will take to age a brew one year")
        long agingYearTicks) {
    public static final BarrelSection DEFAULT = new BarrelSection(Moment.DEFAULT_AGING_YEAR);
}
