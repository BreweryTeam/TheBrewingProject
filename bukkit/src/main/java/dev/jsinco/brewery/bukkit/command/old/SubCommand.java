package dev.jsinco.brewery.bukkit.command.old;

import lombok.Getter;

public enum SubCommand {
    CREATE("brewery.command.create", true, true),
    EVENT("brewery.command.event", true, true),
    STATUS("brewery.command.status", true, false),
    INFO("brewery.command.info", true, true),
    RELOAD("brewery.command.reload", false, false),
    SEAL("brewery.command.seal", true, true);

    @Getter
    private final String permissionNode;
    @Getter
    private final boolean requiresOfflinePlayer;
    @Getter
    private final boolean requiresPlayer;

    SubCommand(String permissionNode, boolean requiresOfflinePlayer, boolean requiresPlayer) {
        this.permissionNode = permissionNode;
        this.requiresOfflinePlayer = requiresOfflinePlayer;
        this.requiresPlayer = requiresPlayer;
    }
}
