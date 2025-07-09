package dev.jsinco.brewery.configuration.locale;

import dev.jsinco.brewery.configuration.AbstractConfig;
import dev.jsinco.brewery.configuration.Config;
import dev.jsinco.brewery.util.FileUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class TranslationsConfig extends AbstractConfig {

    @Key("brew.tooltip-brewing")
    public static Map<String, String> BREW_TOOLTIP_BREWING;

    @Key("brew.tooltip")
    public static Map<String, String> BREW_TOOLTIP;

    @Key("brew.tooltip-sealed")
    public static Map<String, String> BREW_TOOLTIP_SEALED;

    @Key("brew.detailed-tooltip")
    public static Map<String, String> DETAILED_BREW_TOOLTIP;

    @Key("brew.tooltip.alcoholic")
    public static String ALCOHOLIC;

    @Key("brew.tooltip.detailed-alcoholic")
    public static String DETAILED_ALCOHOLIC;

    @Key("brew.tooltip.quality-brewing")
    public static String BREW_TOOLTIP_QUALITY_BREWING;

    @Key("brew.tooltip.quality")
    public static String BREW_TOOLTIP_QUALITY;

    @Key("brew.tooltip.quality-sealed")
    public static String BREW_TOOLTIP_QUALITY_SEALED;

    @Key("brew.tooltip.volume")
    public static String BREW_TOOLTIP_VOLUME;

    @Key("brew.display-name.unfinished-aged")
    public static String BREW_DISPLAY_NAME_UNFINISHED_AGED;

    @Key("brew.display-name.unfinished-aged-unknown")
    public static String BREW_DISPLAY_NAME_UNFINISHED_AGED_UNKNOWN;

    @Key("brew.display-name.unfinished-distilled")
    public static String BREW_DISPLAY_NAME_UNFINISHED_DISTILLED;

    @Key("brew.display-name.unfinished-distilled-unknown")
    public static String BREW_DISPLAY_NAME_UNFINISHED_DISTILLED_UNKNOWN;

    @Key("brew.display-name.unfinished-fermented")
    public static String BREW_DISPLAY_NAME_UNFINISHED_FERMENTED;

    @Key("brew.display-name.unfinished-fermented-unknown")
    public static String BREW_DISPLAY_NAME_UNFINISHED_FERMENTED_UNKNOWN;

    @Key("brew.display-name.unfinished-mixed")
    public static String BREW_DISPLAY_NAME_UNFINISHED_MIXED;

    @Key("brew.display-name.unfinished-mixed-unknown")
    public static String BREW_DISPLAY_NAME_UNFINISHED_MIXED_UNKNOWN;

    @Key("distillery.create")
    public static String DISTILLERY_CREATE;

    @Key("distillery.create-denied")
    public static String DISTILLERY_CREATE_DENIED;

    @Key("distillery.access-denied")
    public static String DISTILLERY_ACCESS_DENIED;

    @Key("barrel.create")
    public static String BARREL_CREATE;

    @Key("barrel.type.none")
    public static String BARREL_TYPE_NONE;

    @Key("barrel.type")
    public static Map<String, String> BARREL_TYPE;

    @Key("barrel.create-denied")
    public static String BARREL_CREATE_DENIED;

    @Key("barrel.access-denied")
    public static String BARREL_ACCESS_DENIED;

    @Key("cauldron.access-denied")
    public static String CAULDRON_ACCESS_DENIED;

    @Key("cauldron.cant-add-more-ingredients")
    public static String CAULDRON_CANT_ADD_MORE_INGREDIENTS;

    @Key("cauldron.type")
    public static Map<String, String> CAULDRON_TYPE;

    @Key("cauldron.type.none")
    public static String CAULDRON_TYPE_NONE;

    @Key("command.illegal-argument-detailed")
    public static String COMMAND_ILLEGAL_ARGUMENT_DETAILED;

    @Key("cauldron.clock-message")
    public static String CAULDRON_CLOCK_MESSAGE;

    @Key("command.create.missing-mandatory-argument")
    public static String COMMAND_CREATE_MISSING_MANDATORY_ARGUMENT;

    @Key("command.create.success")
    public static String COMMAND_CREATE_SUCCESS;

    @Key("command.status.info.message")
    public static String COMMAND_STATUS_INFO_MESSAGE;

    @Key("command.status.consume.message")
    public static String COMMAND_STATUS_CONSUME_MESSAGE;

    @Key("command.status.clear.message")
    public static String COMMAND_STATUS_CLEAR_MESSAGE;

    @Key("command.status.set.message")
    public static String COMMAND_STATUS_SET_MESSAGE;

    @Key("command.unknown-player")
    public static String COMMAND_UNKNOWN_PLAYER;

    @Key("command.undefined-player")
    public static String COMMAND_UNDEFINED_PLAYER;

    @Key("command.not-enough-permissions")
    public static String COMMAND_NOT_ENOUGH_PERMISSIONS;

    @Key("command.info.not-a-brew")
    public static String COMMAND_INFO_NOT_A_BREW;

    @Key("command.info.message")
    public static String COMMAND_INFO_BREW_MESSAGE;

    @Key("command.info.effect-message")
    public static String COMMAND_INFO_EFFECT_MESSAGE;

    @Key("command.reload-message")
    public static String COMMAND_RELOAD_MESSAGE;

    @Key("command.missing-argument")
    public static String COMMAND_MISSING_ARGUMENT;

    @Key("command.illegal-argument")
    public static String COMMAND_ILLEGAL_ARGUMENT;

    @Key("command.seal-success")
    public static String COMMAND_SEAL_SUCCESS;

    @Key("command.seal-failure")
    public static String COMMAND_SEAL_FAILURE;

    @Key("events.default-kick-event-message")
    public static String KICK_EVENT_MESSAGE;

    @Key("events.drunken-join-deny-message")
    public static String DRUNKEN_JOIN_DENY_MESSAGE;

    @Key("events.types")
    public static Map<String, String> EVENT_TYPES;

    @Key("events.nothing-planned")
    public static String NO_EVENT_PLANNED;

    @Key("events.chicken-message")
    public static String CHICKEN_MESSAGE;

    @Key("events.teleport-message")
    public static String TELEPORT_MESSAGE;

    @Key("info.after-drink")
    public static String INFO_AFTER_DRINK;

    private static final TranslationsConfig TRANSLATIONS = new TranslationsConfig();

    public static void reload(File dataFolder) {
        Path mainDir = dataFolder.toPath();

        // extract default config from jar
        String fileName = "locale/" + Config.LANGUAGE + ".yml";
        FileUtil.extractFile(TranslationsConfig.class, fileName, mainDir, false);

        TRANSLATIONS.reload(mainDir.resolve(fileName), fileName, TranslationsConfig.class);
    }
}
