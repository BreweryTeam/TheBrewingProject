package dev.jsinco.brewery.configuration.locale;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public record BrewSection(
        BrewTooltipSection tooltipBrewing,
        BrewTooltipSection tooltip,
        BrewTooltipSection tooltipSealed,
        BrewTooltipSection detailedTooltip,
        String alcoholic,
        String detailedAlcoholic,
        String qualityBrewing,
        String quality,
        @Comment("Clarification; this is the extra message if you seal a brew with a named paper")
        String volume,
        BrewDisplayNameSection displayName
) {

    public static final BrewSection DEFAULT = new BrewSection(
            BrewTooltipSection.DEFAULT,
            BrewTooltipSection.DEFAULT,
            BrewTooltipSection.DEFAULT,
            BrewTooltipSection.DEFAULT,
            "alcoholic",
            "alcoholic - detailed",
            "quality",
            "quality - brewing",
            "volume message",
            BrewDisplayNameSection.DEFAULT
    );
}
