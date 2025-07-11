package dev.jsinco.brewery.configuration.locale;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record StatusCommandSection(
        InfoSection info,
        ConsumeSection consume,
        ClearSection clear,
        SetSection set
) {

    public record InfoSection(String message) {

    }

    public record ConsumeSection(String message) {

    }

    public record ClearSection(String message) {

    }

    public record SetSection(String message) {

    }
}
