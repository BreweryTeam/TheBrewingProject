package dev.jsinco.brewery.configuration.locale;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record InfoCommandSection(
        String message,
        String notABrew,
        String effectMessage
) {
}
