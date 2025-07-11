package dev.jsinco.brewery.configuration.locale;

import dev.jsinco.brewery.util.Registry;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@ConfigSerializable
public record CauldronSection(
        String accessDenied,
        Map<String, String> cauldronType,
        String cauldronClockMessage
) {
    public static final CauldronSection DEFAULT = new CauldronSection(
            "access denied",
            Registry.CAULDRON_TYPE.values().stream()
                    .collect(Collectors.toMap(
                            cauldronType -> cauldronType.toString().toLowerCase(Locale.ROOT),
                            cauldronType -> cauldronType.toString().toLowerCase(Locale.ROOT)
                    )),
            "cauldron clock message"
    );
}
