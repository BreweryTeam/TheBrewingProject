package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.FileUtil;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class Config extends AbstractConfig {
    @Key("config-version")
    @Comment("""
            Config version. Don't change this""")
    public static int CONFIG_VERSION = 0;

    @Key("language")
    @Comment("""
            What language file should we use? See: /TheBrewingProject/languages""")
    public static String LANGUAGE = "en-us";


    // Brewing Settings


    // Storage Settings


    // Cauldron Settings
    @Key("cauldrons.minimal-particles")
    @Comment("""
            Reduce the number of particles that spawn while cauldrons brew.
            This won't affect performance, but it will make the particles less obtrusive.""")
    public static boolean MINIMAL_PARTICLES = false;

    @Key("cauldrons.heat-sources")
    @Comment("""
            What blocks cauldrons must have below them to be able to brew.
            If this list is empty, cauldrons will brew regardless of the block below them.
            Campfires must be lit and lava must be a source block.""")
    public static List<String> HEAT_SOURCES = List.of("campfire", "soul_campfire", "lava", "fire", "soul_fire", "magma_block");

    @Key("cauldrons.cooking-minute-ticks")
    @Comment("How many ticks it will take to cook something one minute")
    public static long COOKING_MINUTE_TICKS = Moment.MINUTE;

    @Key("barrels.aging-year-ticks")
    @Comment("How many ticks it will take to age a brew one year")
    public static long AGING_YEAR_TICKS = Moment.DEFAULT_AGING_YEAR;

    @Key("automation-enabled")
    @Comment("""
            Allow hoppers to interact with distilleries and barrels""")
    public static boolean AUTOMATION = true;

    @Key("puke.despawn-rate")
    @Comment("""
            How many ticks should the puke items live""")
    public static int PUKE_DESPAWN_RATE = 6 * Moment.SECOND;

    @Key("puke.puke-time")
    @Comment("""
            How many ticks the player will puke""")
    public static int PUKE_TIME = 4 * Moment.SECOND;

    @Key("events.kick-event.message")
    public static String KICK_EVENT_MESSAGE;

    @Key("events.kick-event.server-message")
    public static String KICK_EVENT_SERVER_MESSAGE;

    @Key("events.pass-out-time")
    public static int PASS_OUT_TIME = 5;

    @Key("events.messages")
    public static List<String> DRUNK_MESSAGES = List.of();

    @Key("events.custom-events")
    public static Map<String, Object> CUSTOM_EVENTS = Map.of();

    @Key("events.enabled-random-events")
    public static List<String> ENABLED_RANDOM_EVENTS = List.of();

    @Key("events.teleport-destinations")
    public static List<String> TELEPORT_DESTINATIONS = List.of();

    @Key("events.default")
    public static Map<String, Object> DEFAULT_EVENTS = Map.of();

    @Key("events.drunken-join-deny")
    public static boolean DRUNKEN_JOIN_DENY = true;

    @Key("decay-rates.alcohol")
    @Comment("How many ticks until alcohol level decays by 1%")
    public static int ALCOHOL_DECAY_RATE = 200;

    @Key("decay-rates.toxin")
    @Comment("How many ticks until toxin level decays by 1%")
    public static int TOXIN_DECAY_RATE = 400;

    @Key("allow-unregistered-ingredients")
    @Comment("Whether an ingredient can be added into a brew regardless if it's not in any of the recipes")
    public static boolean ALLOW_UNREGISTERED_INGREDIENTS = false;

    @Key("consume-items-in-creative")
    @Comment("Whether items should be consumed when in creative mode when using it on tbp structures")
    public static boolean CONSUME_ITEMS_IN_CREATIVE = false;

    @Key("sounds")
    @Comment("""
            This field accepts either a single sound definition or a list of definitions.
            If a list is provided, one sound will be chosen randomly.
            
            A single sound entry is a string with one of the following formats
             - <sound_id>
             - <sound_id>/<pitch>
             - <sound_id>/<min_pitch>;<max_pitch>
            
            See the default values below for examples""")
    public static Map<String, Object> SOUNDS = Map.of();

    private static final Config CONFIG = new Config();

    public static void reload(File dataFolder) {
        Path mainDir = dataFolder.toPath();

        // extract default config from jar
        FileUtil.extractFile(Config.class, "config.yml", mainDir, false);

        CONFIG.reload(mainDir.resolve("config.yml"), "config.yml", Config.class);
    }

    private static void tryRenamePath(String oldPath, String newPath) {
        YamlFile config = CONFIG.getConfig();
        Object oldValue = config.get(oldPath);
        if (oldValue == null) {
            return; // old default doesn't exist; do nothing
        }
        if (config.get(newPath) != null) {
            return; // new default already set; do nothing
        }
        config.set(newPath, oldValue);
        config.set(oldPath, null);
        CONFIG.save();
    }
}