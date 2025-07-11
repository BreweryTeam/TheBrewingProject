package dev.jsinco.brewery.configuration;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

@ConfigSerializable
public record Config(
        @Comment("""
                Config version. Don't change this""")
        int configVersion,
        @Comment("""
                What language file should we use? See: /TheBrewingProject/languages""")
        String language,
        CauldronSection cauldrons,
        BarrelSection barrels,
        @Comment("""
                Allow hoppers to interact with distilleries and barrels""")
        boolean automation,
        PukeSection puke,
        EventSection events,
        DecayRateSection decayRate,
        @Comment("Whether an ingredient can be added into a brew regardless if it's not in any of the recipes")
        boolean allowUnregisteredIngredients,
        @Comment("Whether items should be consumed when in creative mode when using it on tbp structures")
        boolean consumeItemsInCreative,
        @Comment("""
                This field accepts either a single sound definition or a list of definitions.
                If a list is provided, one sound will be chosen randomly.
                
                A single sound entry is a string with one of the following formats
                 - <sound_id>
                 - <sound_id>/<pitch>
                 - <sound_id>/<min_pitch>;<max_pitch>
                
                See the default values below for examples""")
        SoundSection sounds
) {

    private static final Config DEFAULT = new Config(
            0,
            "en-us",
            CauldronSection.DEFAULT,
            BarrelSection.DEFAULT,
            true,
            PukeSection.DEFAULT,
            EventSection.DEFAULT,
            DecayRateSection.DEFAULT,
            false,
            false,
            SoundSection.DEFAULT
    );

    private static Config instance;

    public static void load(File dataFolder) {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .file(new File(dataFolder, "config.yml"))
                    .build();
            CommentedConfigurationNode node = loader.load();
            Config.instance = node.get(Config.class, DEFAULT);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public static Config config() {
        return instance;
    }
}