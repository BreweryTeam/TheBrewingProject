package dev.jsinco.brewery.paper.config.sections;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;

import java.util.List;

public final class Config extends OkaeriConfig {

    @CustomKey("config-version")
    @Comment({
            "Config version. Don't change this",
            "TODO: Okaeri has built-in migration support, we should use that"
    })
    public String configVersion = "1.0";

    @CustomKey("language")
    @Comment("What language file should we use? See: /TheBrewingProject/languages")
    public String language = "en-us";

    @CustomKey("check-for-updates")
    @Comment({
            "Resolved the latest version of TheBrewingProject and let's",
            "players with the permission node know when an update is available."
    })
    public Boolean checkForUpdates = true;


    @CustomKey("auto-saving")
    @Comment("Auto saving interval in minutes")
    public Integer autoSaveInterval = 9;

    @CustomKey("verbose-logging")
    @Comment("Enable verbose/debug logging")
    public Boolean verboseLogging = false;


    // Brewing Settings


    // Storage Settings


    // Cauldron Settings
    @CustomKey("cauldrons.minimal-particles")
    @Comment({
            "Reduce the number of particles that spawn while cauldrons brew.",
            "This won't affect performance, but it will make the particles less obtrusive."
    })
    public Boolean minimalParticles = false;

    @CustomKey("cauldrons.heat-sources")
    @Comment({
            "What blocks cauldrons must have below them to be able to brew.",
            "If this list is empty, cauldrons will brew regardless of the block below them.",
            "Campfires must be lit and lava must be a source block."
    })
    public List<String> heatSources = List.of("minecraft:campfire", "minecraft:soul_campfire", "minecraft:lava", "minecraft:fire", "minecraft:soul_fire", "minecraft:magma_block");
}
