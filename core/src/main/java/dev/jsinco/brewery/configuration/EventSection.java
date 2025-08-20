package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.event.CustomEvent;
import dev.jsinco.brewery.event.CustomEventRegistry;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.event.step.ApplyPotionEffect;
import dev.jsinco.brewery.event.step.ConditionalWaitStep;
import dev.jsinco.brewery.event.step.ConsumeStep;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.Registry;
import dev.jsinco.brewery.vector.BreweryLocation;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.serdes.OkaeriSerdesPack;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Getter
@Accessors(fluent = true)
public class EventSection extends OkaeriConfig {

    @CustomKey("kick-event")
    private KickEventSection kickEvent = new KickEventSection();

    @Comment("How many game minutes will a player be passed out?")
    @CustomKey("pass-out-time")
    private int passOutTime = 5;

    @Comment("Drunken messages to send if the drunk_message event is enabled (not recommended to have enabled, gets a bit spammy)")
    @CustomKey("messages")
    private List<String> drunkMessages = List.of("I love you <random_player_name>, you're my best friend.",
            "I could do one more.",
            "Who is she?",
            "Watch this!",
            "I'm not drunk. You're drunk.");

    @Comment("Make your own events, see the wiki at https://hangar.papermc.io/BreweryTeam/TheBrewingProject/pages/Wiki/Configuration#-events")
    @CustomKey("custom-events")
    private CustomEventRegistry customEvents = CustomEventRegistry.builder()
            .addEvent(new CustomEvent.Builder()
                    .alcoholRequirement(60)
                    .toxinsRequirement(90)
                    .probabilityWeight(5)
                    .addStep(new EventStep.Builder().addProperty(NamedDrunkEvent.fromKey("pass_out")).build())
                    .addStep(new EventStep.Builder()
                            .addProperty(new ConditionalWaitStep(ConditionalWaitStep.Condition.JOIN))
                            .addProperty(NamedDrunkEvent.fromKey("teleport"))
                            .addProperty(new ConsumeStep(-30, -15))
                            .build()
                    )
                    .build(BreweryKey.parse("memory_loss"))
            ).addEvent(
                    new CustomEvent.Builder()
                            .addStep(new EventStep.Builder().addProperty(new ApplyPotionEffect("darkness",
                                            new Interval(1, 1), new Interval(20 * Moment.SECOND, 20 * Moment.SECOND)
                                    )).build()
                            )
                            .build(BreweryKey.parse("tunnel_vision"))
            ).build();

    @Comment("What events will be randomly chosen over time when the player is drunk")
    @CustomKey("enabled-random-events")
    private List<String> enabledRandomEvents = List.of("puke", "memory_loss", "stumble", "chicken", "nausea", "tunnel_vision", "drunken_walk", "hallucination", "fever", "kaboom");

    @Comment("Teleport destinations for the 'teleport' event")
    @CustomKey("teleport-destinations")
    private List<BreweryLocation.Uncompiled> teleportDestinations = Stream.<BreweryLocation.Uncompiled>of(
            worldUuids -> worldUuids.isEmpty() ? null : new BreweryLocation(0, 80, 0, worldUuids.getFirst())
    ).toList();

    @Comment("Deny joining the server if too drunk")
    @CustomKey("drunken-join-deny")
    private boolean drunkenJoinDeny = true;

    @Comment("Transform text with blurred speech if the player is drunk enough")
    @CustomKey("blurred-speech")
    private boolean blurredSpeech = true;

    @Comment("What upwards velocity the player will get in kaboom event")
    @CustomKey("kaboom-velocity")
    private double kaboomVelocity = 0.2;

    @Comment("Change the properties of premade events")
    @CustomKey("named-drunk-event-overrides")
    private List<NamedDrunkEvent> namedDrunkEventsOverride = Registry.DRUNK_EVENT.values().stream().toList();

    @Getter
    @Accessors(fluent = true)
    public static class KickEventSection extends OkaeriConfig {
        private String kickEventMessage = null;
        private String kickServerMessage = null;
    }

    @Exclude
    private static EventSection instance;

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
