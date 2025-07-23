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
import dev.jsinco.brewery.vector.BreweryLocation;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Accessors(fluent = true)
public class EventSection extends OkaeriConfig {

    @CustomKey("kick-event")
    private KickEventSection kickEvent = new KickEventSection();

    @Comment("How many game minutes will a player be passed out?")
    @CustomKey("pass-out-time")
    private int passOutTime = 5;

    @Comment("Drunken messages to send if the drunk_message event is enabled (not recommended to have enabled, gets a bit spammy)")
    @CustomKey("drunk-messages")
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
    private List<BreweryLocation.Uncompiled> teleportDestinations = List.of(worldUuids -> new BreweryLocation(0, 80, 0, worldUuids.get(0)));

    @Comment("Deny joining the server if too drunk")
    @CustomKey("drunken-join-deny")
    private boolean drunkenJoinDeny = true;

    @Comment("Transform text with blurred speech if the player is drunk enough")
    @CustomKey("blurred-speech")
    private boolean blurredSpeech = true;

    @Getter
    @Accessors(fluent = true)
    public static class KickEventSection extends OkaeriConfig {
        private String kickEventMessage = null;
        private String kickServerMessage = null;
    }
}
