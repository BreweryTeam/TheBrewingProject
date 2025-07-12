package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class PukeSection {

    @Comment("How many ticks should the puke items live")
    public int pukeDespawnTime = 6 * Moment.SECOND;

    @Comment("How many ticks the player will puke")
    public int pukeTime = 4 * Moment.SECOND;
}
