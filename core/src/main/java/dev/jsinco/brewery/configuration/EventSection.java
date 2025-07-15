package dev.jsinco.brewery.configuration;

import dev.jsinco.brewery.event.*;
import dev.jsinco.brewery.moment.Interval;
import dev.jsinco.brewery.moment.Moment;
import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.vector.BreweryLocation;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.function.Supplier;

@ConfigSerializable
@Getter
@Accessors(fluent = true)
public class EventSection {
    private KickEventSection kickEvent = new KickEventSection();
    private int passOutTime = 5;
    private List<String> drunkMessages = List.of("I love you <random_player_name>, you're my best friend.",
            "I could do one more.",
            "Who is she?",
            "Watch this!",
            "I'm not drunk. You're drunk.");
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
    private List<String> enabledRandomEvents = List.of("puke", "memory_loss", "stumble", "chicken", "nausea", "tunnel_vision", "drunken_walk");
    private List<Supplier<BreweryLocation>> teleportDestinations = List.of();
    private boolean drunkenJoinDeny = true;

    @ConfigSerializable
    @Getter
    @Accessors(fluent = true)
    public static class KickEventSection {
        private String kickEventMessage = null;
        private String kickServerMessage = null;
    }
}
