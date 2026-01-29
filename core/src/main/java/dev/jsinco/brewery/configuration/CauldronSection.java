package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.api.moment.Moment;
import dev.jsinco.brewery.api.util.Holder;
import dev.jsinco.brewery.api.config.Configuration;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.format.NamedTextColor;

import java.awt.*;
import java.util.List;
import java.util.stream.Stream;

@Getter
@Accessors(fluent = true)
public class CauldronSection extends OkaeriConfig implements Configuration.Cauldrons {
    @Comment({"Reduce the number of particles that spawn while cauldrons brew.",
            "This won't affect performance, but it will make the particles less obtrusive."})
    @CustomKey("minimal-particles")
    private boolean minimalParticles = false;

    @Comment({"What blocks cauldrons must have below them to be able to brew.",
            "If this list is empty, cauldrons will brew regardless of the block below them.",
            "Campfires must be lit and lava must be a source block."})
    @CustomKey("heat-sources")
    private List<Holder.Material> heatSources = Stream.of("campfire", "soul_campfire", "lava", "fire", "soul_fire", "magma_block")
            .map(Holder.Material::fromMinecraftId)
            .toList();

    @Comment("How many ticks it will take to cook something one minute")
    @CustomKey("cooking-minute-ticks")
    private long cookingMinuteTicks = Moment.MINUTE;

    @Comment("The base color lava cauldrons have")
    @CustomKey("lava-base-particle-color")
    private Color lavaBaseParticleColor = new Color(Integer.parseInt("d45a12", 16));

    @Comment("The base color water cauldrons have")
    @CustomKey("water-base-particle-color")
    private Color waterBaseParticleColor = new Color(NamedTextColor.AQUA.value());

    @Comment("The base color snow cauldrons have")
    @CustomKey("snow-base-particle-color")
    private Color snowBaseParticleColor = new Color(Integer.parseInt("f8fdfd", 16));

    @Comment("The base color snow cauldrons have")
    @CustomKey("failed-particle-color")
    private Color failedParticleColor = new Color(NamedTextColor.GRAY.value());

    @Comment("If an animation should be run when adding an ingredient")
    @CustomKey("ingredient-added-animation")
    private boolean ingredientAddedAnimation = true;
}
