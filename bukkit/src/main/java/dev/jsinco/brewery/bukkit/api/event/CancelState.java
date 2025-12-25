package dev.jsinco.brewery.bukkit.api.event;

import net.kyori.adventure.text.Component;

public interface CancelState {

    record Cancelled() implements CancelState {
    }

    record PermissionDenied(Component message) implements CancelState {
    }

    record Allowed() implements CancelState {
    }
}
