package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Getter
@Accessors(fluent = true)
public class PukeSection {

    @Comment("How many ticks should the puke items live")
    private int pukeDespawnTime = 6 * Moment.SECOND;

    @Comment("How many ticks the player will puke")
    private int pukeTime = 4 * Moment.SECOND;
}
