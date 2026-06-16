package dev.jsinco.brewery.api.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

public sealed interface CancelState {

    /**
     * Send a message to the audience
     *
     * @param audience The audience
     */
    void sendMessage(@Nullable Audience audience);

    /**
     * The action should cancel silently
     */
    record Cancelled() implements CancelState {
        @Override
        public void sendMessage(@Nullable Audience audience) {
            // NO-OP
        }
    }

    /**
     * The action should cancel with a message
     *
     * @param message Cancel message
     */
    record PermissionDenied(Component message) implements CancelState {

        @Override
        public void sendMessage(@Nullable Audience audience) {
            if (audience == null) {
                return;
            }
            audience.sendMessage(message);
        }
    }

    /**
     * The action should continue silently
     */
    record Allowed() implements CancelState {
        @Override
        public void sendMessage(@Nullable Audience audience) {
            // NO-OP
        }
    }
}
