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

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

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

    @Comment({"Some clients and tools allow players to see the NBT data of an item, which, for our potions, includes",
            "all the brewing steps it went through. Encrypt this data to prevent it from being abused as a recipe?"})
    @CustomKey("encrypt-sensitive-data")
    private boolean encryptSensitiveData = true;

    @Comment("The key that is going to be used for the encryption, this is unique per server")
    private SecretKey encryptionKey = generateAesKey();

    @Comment("A list of previous keys to try when decryption with the current one fails")
    private List<SecretKey> previousEncryptionKeys = List.of();

    @Comment({"Should we re-encrypt all items in opened inventories to use the newest encryption standard and the latest key?",
            "If your encryption key got leaked, use this to prevent anyone from seeing items encrypted with old keys ever again"})
    private boolean reencryptItemsInInventories = false;

    @CustomKey("empty-any-drink-using-hopper")
    @Comment("Empty any drink when right-clicking a hopper. If false, only applies to failed brews")
    private boolean emptyAnyDrinkUsingHopper = false;

    @CustomKey("cauldrons")
    private CauldronSection cauldrons = new CauldronSection();

    @CustomKey("barrels")
    private BarrelSection barrels = new BarrelSection();

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
    @Comment("The aliases for the 'tbp' command")
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

    public static SecretKey generateAesKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            try {
                kg.init(256, SecureRandom.getInstanceStrong());
            } catch (Exception ignored) {
                kg.init(128, new SecureRandom());
            }
            return kg.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static SecretKey generateDesKey() {
        try {
            return KeyGenerator.getInstance("DES").generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}