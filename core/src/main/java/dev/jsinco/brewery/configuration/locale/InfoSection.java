package dev.jsinco.brewery.configuration.locale;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record InfoSection(
        String afterDrink
) {
}
