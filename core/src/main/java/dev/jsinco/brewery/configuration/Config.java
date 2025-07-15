package dev.jsinco.brewery.configuration;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.util.MapFactories;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

@ConfigSerializable
@Getter
@Accessors(fluent = true)
public class Config {
    public static final int CONFIG_INDENT = 2;


    @Comment("Config version. Don't change this")
    private int configVersion = 0;

    @Comment("What language file should we use? See: /TheBrewingProject/locale")
    private String language = "en-us";

    @Comment("Allow hoppers to interact with distilleries and barrels")
    private boolean automation = true;

    @Comment("Whether an ingredient can be added into a brew regardless if it's not in any of the recipes")
    private boolean allowUnregisteredIngredients = false;

    @Comment("Whether items should be consumed when in creative mode when using it on tbp structures")
    private boolean consumeItemsInCreative = false;

    private CauldronSection cauldrons = new CauldronSection();

    private BarrelSection barrels = new BarrelSection();

    private PukeSection puke = new PukeSection();

    private EventSection events = new EventSection();

    private DecayRateSection decayRate = new DecayRateSection();

    @Comment("""
            This field accepts either a single sound definition or a list of definitions.
            If a list is provided, one sound will be chosen randomly.
            
            A single sound entry is a string with one of the following formats
             - <sound_id>
             - <sound_id>/<pitch>
             - <sound_id>/<min_pitch>;<max_pitch>
            
            See the default values below for examples""")
    private SoundSection sounds = new SoundSection();

    private static Config instance;

    public static void load(File dataFolder, TypeSerializerCollection serializers) {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .file(new File(dataFolder, "config.yml"))
                    .defaultOptions(opts -> opts.serializers(build -> build.registerAll(serializers))
                            .mapFactory(MapFactories.insertionOrdered())
                    )
                    .nodeStyle(NodeStyle.BLOCK)
                    .indent(CONFIG_INDENT)
                    .build();
            CommentedConfigurationNode node = loader.load();
            Config.instance = node.get(Config.class);
            node.set(Config.class, instance);
            loader.save(node);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    public static Config config() {
        return instance;
    }
}