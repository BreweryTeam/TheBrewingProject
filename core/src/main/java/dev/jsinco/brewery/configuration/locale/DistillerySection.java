package dev.jsinco.brewery.configuration.locale;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record DistillerySection(
        String create,
        String createDenied,
        String accessDenied
) {

    public static final DistillerySection DEFAULT = new DistillerySection(
            "create",
            "create denied",
            "access denied"
    );
}
