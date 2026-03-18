package dev.jsinco.brewery.api.event;

import dev.jsinco.brewery.api.util.Holder;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public interface IntegrationEvent extends EventStepProperty, DrunkEvent {

    /**
     * Simple way to run the event
     * @param player The player target for event
     */
    void run(Holder.Player player);

    /**
     * Complex way to run the event
     * @return An event executable
     */
    default @NonNull EventPropertyExecutable toExecutable() {
        return new EventPropertyExecutable() {
            @Override
            public @NonNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
                run(new Holder.Player(contextPlayer));
                return ExecutionResult.CONTINUE;
            }

            @Override
            public int priority() {
                return 42; // The meaning of life
            }
        };
    }
}
