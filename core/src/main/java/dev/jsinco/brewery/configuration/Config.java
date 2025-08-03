package dev.jsinco.brewery.configuration;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.Locale;
import java.util.List;

@Getter
@Accessors(fluent = true)
public class Config extends OkaeriConfig {

    @Comment("Config version. Don't change this")
    @CustomKey("config-version")
    private int configVersion = 1;

    @Comment("What language file should we use? See: /TheBrewingProject/locale")
    @CustomKey("language")
    private Locale language = Locale.US;

    @Comment("Allow hoppers to interact with distilleries and barrels")
    @CustomKey("automation-enabled")
    private boolean automation = true;

    @Comment("Whether an ingredient can be added into a brew regardless if it's not in any of the recipes")
    @CustomKey("allow-unregistered-ingredients")
    private boolean allowUnregisteredIngredients = false;

    @Comment("Whether items should be consumed when in creative mode when using it on tbp structures")
    @CustomKey("consume-items-in-creative")
    private boolean consumeItemsInCreative = false;

    @Comment("Whether everything non-item related should be translated to the players locale")
    @CustomKey("client-side-translations")
    private boolean clientSideTranslations = false;

    @CustomKey("cauldrons")
    private CauldronSection cauldrons = new CauldronSection();

    @CustomKey("barrels")
    private BarrelSection barrels = new BarrelSection();

    @CustomKey("puke")
    private PukeSection puke = new PukeSection();

    @CustomKey("events")
    private EventSection events = new EventSection();

    @CustomKey("decay-rates")
    private DecayRateSection decayRate = new DecayRateSection();

    @Comment({"This field accepts either a single sound definition or a list of definitions.",
            "If a list is provided, one sound will be chosen randomly.",
            "",
            "A single sound entry is a string with one of the following formats",
            "- <sound_id>",
            "- <sound_id>/<pitch>",
            "- <sound_id>/<min_pitch>;<max_pitch>",
            "",
            "See the default values below for examples"
    })
    @CustomKey("sounds")
    private SoundSection sounds = new SoundSection();


    @CustomKey("command-aliases")
    private List<String> commandAliases = List.of("brewery", "brew");

    @Exclude
    private static Config instance;

    public static void load(File dataFolder, OkaeriSerdesPack... packs) {
        Config.instance = ConfigManager.create(Config.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer(), packs);
            it.withBindFile(new File(dataFolder, "config.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
    }

    public static Config config() {
        return instance;
    }
}