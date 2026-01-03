package dev.jsinco.brewery.api.util;

import net.kyori.adventure.text.Component;

public sealed interface CancelState {

    record Cancelled() implements CancelState {
    }

    record PermissionDenied(Component message) implements CancelState {
    }

    record Allowed() implements CancelState {
    }
}
