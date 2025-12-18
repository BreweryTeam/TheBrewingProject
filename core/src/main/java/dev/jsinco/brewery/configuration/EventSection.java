package dev.jsinco.brewery.configuration;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.api.effect.modifier.ModifierExpression;
import dev.jsinco.brewery.api.event.*;
import dev.jsinco.brewery.api.event.step.*;
import dev.jsinco.brewery.api.math.RangeD;
import dev.jsinco.brewery.api.moment.Interval;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;
import dev.jsinco.brewery.api.util.Logger;
import dev.jsinco.brewery.api.vector.BreweryLocation;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.time.Duration;
import dev.jsinco.brewery.time.TimeUtil;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Getter
@Accessors(fluent = true)
public class EventSection extends OkaeriConfig {

    @CustomKey("kick-event")
    private KickEventSection kickEvent = new KickEventSection();

    @Comment("How long will a player be passed out?")
    @CustomKey("pass-out-time")
    private Duration.Minutes passOutTime = new Duration.Minutes(TimeUtil.parse("5min"));

    @Comment("Drunken messages to send if the drunk_message event is enabled (not recommended to have enabled, gets a bit spammy)")
    @CustomKey("messages")
    private List<String> drunkMessages = List.of("I love you <random_player_name>, you're my best friend.",
            "I could do one more.",
            "Who is she?",
            "Watch this!",
            "I'm not drunk. You're drunk.");

    @CustomKey("puke")
    private PukeSection puke = new PukeSection();

    @Comment("What events will be randomly chosen over time when the player is drunk")
    @CustomKey("enabled-random-events")
    private List<String> enabledRandomEvents = List.of("puke", "memory_loss", "stumble", "chicken", "nausea", "tunnel_vision", "drunken_walk", "hallucination", "fever", "kaboom");

    @Comment("Teleport destinations for the 'teleport' event")
    @CustomKey("teleport-destinations")
    private List<BreweryLocation.Uncompiled> teleportDestinations = List.of(new BreweryLocation.Uncompiled(0, 70, 0, "world"));

    @Comment("Whether we should find a safe location at the requested coordinates")
    @CustomKey("ensure-safe-location")
    private boolean ensureSafeLocation = true;

    @Comment({"Should we randomly offset target locations inside a given radius?", "Set to -1 to use the world's spawn radius"})
    @CustomKey("random-offset-radius")
    private int randomOffsetRadius = -1;

    @Comment("How likely should it be that the target location is chosen underground? (%)")
    @CustomKey("underground-chance")
    private int undergroundChance = 25;

    @Comment("Deny joining the server if too drunk")
    @CustomKey("drunken-join-deny-event")
    private DrunkenJoinEvent drunkenJoinDeny = new DrunkenJoinEvent();

    @Comment("Transform text with blurred speech if the player is drunk enough")
    @CustomKey("blurred-speech")
    private boolean blurredSpeech = true;

    @Comment("What upwards velocity the player will get in the kaboom event")
    @CustomKey("kaboom-velocity")
    private double kaboomVelocity = 0.2;

    @Comment({"What health players should be set to in the kaboom event",
            "This will not heal them when they have less HP"})
    @CustomKey("kaboom-health")
    private double kaboomHealth = 12.0;

    @Comment("The duration where player freezes for the fever event")
    @CustomKey("fever-freezing-time")
    private Duration.Ticks feverFreezingTime = new Duration.Ticks(200);

    @Comment("The duration where player freezes for the fever event")
    @CustomKey("fever-burning-time")
    private Duration.Ticks feverBurnTime = new Duration.Ticks(100);

    @Comment("Make your own events, see the wiki at https://hangar.papermc.io/BreweryTeam/TheBrewingProject/pages/Wiki/Configuration#-events")
    @CustomKey("custom-events")
    private CustomEventRegistry customEvents = CustomEventRegistry.builder()
            .addEvent(new CustomEvent.Builder()
                    .probability(new EventProbability(new ModifierExpression("0.5 * probabilityWeight(alcohol)"), Map.of("alcohol", new RangeD(90D, null))))
                    .displayName(Component.text("loose memory"))
                    .addStep(new EventStep.Builder().addProperty(NamedDrunkEvent.fromKey("pass_out")).build())
                    .addStep(new EventStep.Builder()
                            .addProperty(new ConditionalWaitStep(new Condition.JoinedServer()))
                            .addProperty(NamedDrunkEvent.fromKey("teleport"))
                            .addProperty(new ConsumeStep(Map.of(DrunkenModifierSection.modifiers().modifier("alcohol"), -30D)))
                            .build()
                    )
                    .build(BreweryKey.parse("memory_loss"))
            ).addEvent(new CustomEvent.Builder()
                    .displayName(Component.text("get tunnel vision"))
                    .probability(new EventProbability(new ModifierExpression("probabilityWeight(alcohol)"), Map.of("alcohol", new RangeD(40D, null))))
                    .addStep(new EventStep.Builder().addProperty(new ApplyPotionEffect("darkness",
                                    new Interval(1, 1), new Interval(20, 20)
                            )).build()
                    )
                    .build(BreweryKey.parse("tunnel_vision"))
            ).addEvent(new CustomEvent.Builder()
                    .displayName(Component.text("feel alcohol withdrawal"))
                    .probability(new EventProbability(new ModifierExpression("probabilityWeight(alcohol_addiction)"),
                            Map.of("alcohol", new RangeD(null, 20D),
                                    "alcohol_addiction", new RangeD(50D, null)))
                    ).addStep(
                            new EventStep.Builder().addProperty(new ApplyPotionEffect("poison", new Interval(1, 1), new Interval(20, 20)))
                                    .addProperty(new SendCommand("title @player_name@ actionbar {text:\"You are experiencing alcohol withdrawal\",color:\"gray\"}", SendCommand.CommandSenderType.SERVER))
                                    .build()
                    ).build(BreweryKey.parse("drinking_addiction"))
            ).build();

    @Comment("Change the properties of premade events")
    @CustomKey("named-drunk-event-overrides")
    private List<NamedDrunkEvent> namedDrunkEventsOverride = BreweryRegistry.DRUNK_EVENT.values().stream().toList();

    @Exclude
    private static EventSection instance;

    public static void validate() {
        Preconditions.checkState(instance != null, "Instance can not be null");
        Map<String, Double> variables = new DrunkStateImpl(0, -1).asVariables();
        boolean noneFailed = true;
        for (CustomEvent.Keyed customEvent : instance.customEvents().events()) {
            noneFailed &= validateEvent(variables, customEvent.probability(), customEvent.key());
        }
        for (NamedDrunkEvent namedDrunkEvent : instance.namedDrunkEventsOverride()) {
            noneFailed &= validateEvent(variables, namedDrunkEvent.probability(), namedDrunkEvent.key());
        }
        Preconditions.checkState(noneFailed, "An event has failed validation, please check above exception");
    }

    private static boolean validateEvent(Map<String, Double> variables, EventProbability probability, BreweryKey key) {
        try {
            probability.evaluate(variables);
            for (String modifierName : probability.allowedRanges().keySet()) {
                DrunkenModifierSection.modifiers().modifier(modifierName);
            }
            return true;
        } catch (Exception e) {
            Logger.logErr("Invalid event: " + key);
            Logger.logErr(e);
            return false;
        }
    }

    @Getter
    @Accessors(fluent = true)
    public static class KickEventSection extends OkaeriConfig {
        @CustomKey("kick-event-message")
        @Comment("The message to send to the player when getting kicked through the passout event")
        private String kickEventMessage = null;

        @CustomKey("kick-server-message")
        @Comment("THe message to send to all players when a player gets kicked through the passout event")
        private String kickServerMessage = null;
    }

    @Getter
    @Accessors(fluent = true)
    public static class DrunkenJoinEvent extends OkaeriConfig {
        private boolean enabled = true;
        private EventProbability probability = new EventProbability(new ModifierExpression("85 - alcohol"), Map.of("alcohol", new RangeD(85D, null)));
    }

    public static EventSection events() {
        return instance;
    }

    public static void load(File dataFolder, OkaeriSerdesPack... packs) {
        EventSection.instance = ConfigManager.create(EventSection.class, it -> {
            it.withConfigurer(new YamlSnakeYamlConfigurer(), packs);
            it.withBindFile(new File(dataFolder, "events.yml"));
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
    }

    public static void migrateEvents(File dataFolder) {
        Yaml yaml = new Yaml();
        Object output;
        File configFile = new File(dataFolder, "config.yml");
        if (!configFile.exists()) {
            return;
        }
        try (InputStream inputStream = new FileInputStream(configFile)) {
            Map<String, Object> config = yaml.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            if (config == null) {
                return;
            }
            output = config.get("events");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (output == null) {
            return;
        }
        try (OutputStream outputStream = new FileOutputStream(new File(dataFolder, "events.yml"))) {
            yaml.dump(output, new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
