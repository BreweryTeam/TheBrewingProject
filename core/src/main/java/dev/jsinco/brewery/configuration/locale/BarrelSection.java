package dev.jsinco.brewery.configuration.locale;

import dev.jsinco.brewery.util.Registry;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public record BarrelSection(
        String create,
        Map<String, String> barrelType,
        String createDenied,
        String accessDenied
) {

    public static final BarrelSection DEFAULT = new BarrelSection(
            "create",
            Registry.BARREL_TYPE.values().stream()
                    .collect(Collectors.toMap(
                            barrelType -> barrelType.name().toLowerCase(Locale.ROOT),
                            barrelType -> barrelType.name().toLowerCase(Locale.ROOT))
                    ),
            "create denied",
            "access denied"
    );

}
