package dev.jsinco.brewery.configuration;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

@ConfigSerializable
public class Config {
    @Comment("Config version. Don't change this")
    public int configVersion = 0;

    @Comment("What language file should we use? See: /TheBrewingProject/languages")
    public String language = "en-us";

    public CauldronSection cauldrons = new CauldronSection();

    public BarrelSection barrels = new BarrelSection();

    @Comment("Allow hoppers to interact with distilleries and barrels")
    public boolean automation = true;

    public PukeSection puke = new PukeSection();

    public EventSection events = new EventSection();

    public DecayRateSection decayRate = new DecayRateSection();

    @Comment("Whether an ingredient can be added into a brew regardless if it's not in any of the recipes")
    public boolean allowUnregisteredIngredients = false;

    @Comment("Whether items should be consumed when in creative mode when using it on tbp structures")
    public boolean consumeItemsInCreative = false;

    @Comment("""
            This field accepts either a single sound definition or a list of definitions.
            If a list is provided, one sound will be chosen randomly.
            
            A single sound entry is a string with one of the following formats
             - <sound_id>
             - <sound_id>/<pitch>
             - <sound_id>/<min_pitch>;<max_pitch>
            
            See the default values below for examples""")
    public SoundSection sounds = new SoundSection();

    private static Config instance;

    public static void load(File dataFolder, TypeSerializerCollection serializers) {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .file(new File(dataFolder, "config.yml"))
                    .defaultOptions(opts -> opts.serializers(build -> build.registerAll(serializers)))
                    .nodeStyle(NodeStyle.BLOCK)
                    .indent(2)
                    .build();
            CommentedConfigurationNode node = loader.load();
            Config.instance = node.get(Config.class);
            loader.save(node);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public static Config config() {
        return instance;
    }
}