package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.event.*;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.BreweryKey;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public record EventSection(KickEventSection kickEvent, int passOutTime, List<String> drunkMessages,
                           CustomEventRegistry customEvents, List<String> enabledRandomEvents,
                           List<String> teleportDestinations, boolean drunkenJoinDeny) {

    public static final EventSection DEFAULT = new EventSection(KickEventSection.DEFAULT,
            5,
            List.of("I love you <random_player_name>, you're my best friend.",
                    "I could do one more.",
                    "Who is she?",
                    "Watch this!",
                    "I'm not drunk. You're drunk."),
            CustomEventRegistry.builder()
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
                    ).build(),
            List.of("puke", "memory_loss", "stumble", "chicken", "nausea", "tunnel_vision", "drunken_walk"),
            List.of("world, 0, 80, 0"),
            true
    );

    public record KickEventSection(String kickEventMessage, String kickServerMessage) {
        public static final KickEventSection DEFAULT = new KickEventSection(null, null);
    }
}
