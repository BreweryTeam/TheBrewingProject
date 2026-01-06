package dev.jsinco.brewery.api.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public sealed interface CancelState {

    record Cancelled() implements CancelState {
    }

    record PermissionDenied(Component message) implements CancelState {

        public void sendMessage(@Nullable Audience audience) {
            if(audience == null) {
                return;
            }
            audience.sendMessage(message);
        }
    }

    record Allowed() implements CancelState {
    }
}
