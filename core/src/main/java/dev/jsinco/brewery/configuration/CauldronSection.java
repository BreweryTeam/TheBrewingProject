package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;

@ConfigSerializable
public record CauldronSection(
        @Comment("""
                Reduce the number of particles that spawn while cauldrons brew.
                This won't affect performance, but it will make the particles less obtrusive.""")
        boolean minimalParticles,
        @Comment("""
                What blocks cauldrons must have below them to be able to brew.
                If this list is empty, cauldrons will brew regardless of the block below them.
                Campfires must be lit and lava must be a source block.""")
        List<String> heatSources,
        @Comment("How many ticks it will take to cook something one minute")
        long cookingMinuteTicks) {

    public static final CauldronSection DEFAULT = new CauldronSection(false, List.of("campfire", "soul_campfire", "lava", "fire", "soul_fire", "magma_block"), Moment.MINUTE);
}
