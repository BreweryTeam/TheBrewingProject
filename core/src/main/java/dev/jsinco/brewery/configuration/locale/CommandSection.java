package dev.jsinco.brewery.configuration.locale;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record CommandSection(
        String illegalArgumentDetailed,
        CreateCommandSection create,
        InfoCommandSection info,
        StatusCommandSection status,
        String unknownPlayer,
        String undefinedPlayer,
        String notEnoughPermissions,
        String reloadMessage,
        String missingArgument,
        String illegalArgument,
        String sealSuccess,
        String sealFailure
) {
    public static final CommandSection DEFAULT = new CommandSection(
            "illegal argument detailed",
            new CreateCommandSection("success"),
            new InfoCommandSection("info", "not a brew", "effect message"),
            new StatusCommandSection(
                    new StatusCommandSection.InfoSection("status info"),
                    new StatusCommandSection.ConsumeSection("status consume"),
                    new StatusCommandSection.ClearSection("status clear"),
                    new StatusCommandSection.SetSection("status set")
            ),
            "unknown player",
            "undefined player",
            "not enough permissions",
            "reload message",
            "missing argument",
            "illegal argument",
            "seal success",
            "seal failure"
    );
}
