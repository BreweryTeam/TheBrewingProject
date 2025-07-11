package dev.jsinco.brewery.configuration.locale;

import dev.jsinco.brewery.util.Registry;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@ConfigSerializable
public record EventSection(
        String defaultKickEventMessage,
        String drunkenJoinDenyMessage,
        Map<String, String> types,
        String nothingPlanned,
        String chickenMessage,
        String teleportMessage
) {

    public static final EventSection DEFAULT = new EventSection(
            "default kick event message",
            "drunken join deny message",
            Registry.DRUNK_EVENT.values().stream()
                    .collect(Collectors.toMap(
                            drunkEvent -> drunkEvent.toString().toLowerCase(Locale.ROOT),
                            drunkEvent -> drunkEvent.name().toLowerCase(Locale.ROOT)
                    )),
            "nothing planned",
            "chicken",
            "teleport"
    );
}
