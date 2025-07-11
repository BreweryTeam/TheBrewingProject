package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public record PukeSection(
        @Comment("""
                How many ticks should the puke items live""")
        int pukeDespawnTime,
        @Comment("""
                How many ticks the player will puke""")
        int pukeTime) {
    public static final PukeSection DEFAULT = new PukeSection(6 * Moment.SECOND, 4 * Moment.SECOND);
}
