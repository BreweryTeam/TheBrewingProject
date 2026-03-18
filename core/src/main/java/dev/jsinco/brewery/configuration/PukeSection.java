package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.time.Duration;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;

public class PukeSection extends OkaeriConfig {

    @Comment("How long should the puke items live")
    @CustomKey("despawn-rate")
    private Duration.Ticks pukeDespawnTime = new Duration.Ticks(6 * Moment.SECOND);

    @Comment("How long should the player puke")
    @CustomKey("puke-time")
    private Duration.Ticks pukeTime = new Duration.Ticks(4 * Moment.SECOND);

    public Duration.Ticks pukeDespawnTime() {
        return pukeDespawnTime;
    }

    public Duration.Ticks pukeTime() {
        return pukeTime;
    }
}
