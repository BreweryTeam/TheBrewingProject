package dev.jsinco.brewery.api.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

public sealed interface CancelState {

    void sendMessage(@Nullable Audience audience);

    record Cancelled() implements CancelState {
        @Override
        public void sendMessage(@Nullable Audience audience) {
            // NO-OP
        }
    }

    record PermissionDenied(Component message) implements CancelState {

        @Override
        public void sendMessage(@Nullable Audience audience) {
            if (audience == null) {
                return;
            }
            audience.sendMessage(message);
        }
    }

    record Allowed() implements CancelState {
        @Override
        public void sendMessage(@Nullable Audience audience) {

        }
    }
}
