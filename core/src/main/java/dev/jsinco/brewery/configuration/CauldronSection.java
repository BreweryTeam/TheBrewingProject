package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.Holder;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Stream;

@Getter
@Accessors(fluent = true)
public class CauldronSection extends OkaeriConfig {
    @Comment("Reduce the number of particles that spawn while cauldrons brew.")
    @Comment("This won't affect performance, but it will make the particles less obtrusive.")
    @CustomKey("minimal-particles")
    private boolean minimalParticles = false;

    @Comment("What blocks cauldrons must have below them to be able to brew.")
    @Comment("If this list is empty, cauldrons will brew regardless of the block below them.")
    @Comment("Campfires must be lit and lava must be a source block.")
    @CustomKey("heat-sources")
    private List<Holder.Material> heatSources = Stream.of("campfire", "soul_campfire", "lava", "fire", "soul_fire", "magma_block")
            .map(Holder.Material::fromMinecraftId)
            .toList();

    @Comment("How many ticks it will take to cook something one minute")
    @CustomKey("cooking-minute-ticks")
    private long cookingMinuteTicks = Moment.MINUTE;

}
