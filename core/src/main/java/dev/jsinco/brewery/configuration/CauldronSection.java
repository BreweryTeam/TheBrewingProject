package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.Holder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.stream.Stream;

@ConfigSerializable
@Getter
@Accessors(fluent = true)
public class CauldronSection {
    @Comment("""
            Reduce the number of particles that spawn while cauldrons brew.
            This won't affect performance, but it will make the particles less obtrusive.""")
    private boolean minimalParticles = false;

    @Comment("""
            What blocks cauldrons must have below them to be able to brew.
            If this list is empty, cauldrons will brew regardless of the block below them.
            Campfires must be lit and lava must be a source block.""")
    private List<Holder.Material> heatSources = Stream.of("campfire", "soul_campfire", "lava", "fire", "soul_fire", "magma_block")
            .map(Holder.Material::fromMinecraftId)
            .toList();

    @Comment("How many ticks it will take to cook something one minute")
    private long cookingMinuteTicks = Moment.MINUTE;

}
