package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.event.*;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.vector.BreweryLocation;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.function.Supplier;

@ConfigSerializable
@Getter
@Accessors(fluent = true)
public class EventSection {
    private KickEventSection kickEvent = new KickEventSection();
    @Comment("How many game minutes will a player be passed out?")
    private int passOutTime = 5;
    @Comment("Drunken messages to send if the drunk_message event is enabled (not recommended to have enabled, gets a bit spammy)")
    private List<String> drunkMessages = List.of("I love you <random_player_name>, you're my best friend.",
            "I could do one more.",
            "Who is she?",
            "Watch this!",
            "I'm not drunk. You're drunk.");
    @Comment("Make your own events, see the wiki at https://hangar.papermc.io/BreweryTeam/TheBrewingProject/pages/Wiki/Configuration#-events")
    private CustomEventRegistry customEvents = CustomEventRegistry.builder()
            .addEvent(new CustomEvent.Builder(BreweryKey.parse("memory_loss"))
                    .alcoholRequirement(60)
                    .toxinsRequirement(90)
                    .probabilityWeight(5)
                    .addStep(NamedDrunkEvent.PASS_OUT)
                    .addStep(new ConditionalWaitStep(ConditionalWaitStep.Condition.JOIN))
                    .addStep(NamedDrunkEvent.TELEPORT)
                    .addStep(new ConsumeStep(-30, -15))
                    .build()
            ).addEvent(
                    new CustomEvent.Builder(BreweryKey.parse("tunnel_vision"))
                            .addStep(new ApplyPotionEffect("darkness",
                                    new Interval(1, 1), new Interval(20 * Moment.SECOND, 20 * Moment.SECOND)
                            )).build()
            ).build();
    @Comment("What events will be randomly chosen over time when the player is drunk")
    private List<String> enabledRandomEvents = List.of("puke", "memory_loss", "stumble", "chicken", "nausea", "tunnel_vision", "drunken_walk");
    @Comment("Teleport destinations for the 'teleport' event")
    private List<Supplier<BreweryLocation>> teleportDestinations = List.of();
    @Comment("Deny joining the server if too drunk")
    private boolean drunkenJoinDeny = true;
    @Comment("Transform text with blurred speech if the player is drunk enough")
    private boolean blurredSpeech = true;

    @ConfigSerializable
    @Getter
    @Accessors(fluent = true)
    public static class KickEventSection {
        private String kickEventMessage = null;
        private String kickServerMessage = null;
    }
}
