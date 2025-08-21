package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class PukeSection extends OkaeriConfig {

    @Comment("How many ticks should the puke items live")
    @CustomKey("despawn-rate")
    private int pukeDespawnTime = 6 * Moment.SECOND;

    @Comment("How many ticks the player will puke")
    @CustomKey("puke-time")
    private int pukeTime = 4 * Moment.SECOND;
}
