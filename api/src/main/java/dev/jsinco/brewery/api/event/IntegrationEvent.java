package dev.jsinco.brewery.api.event;

import dev.jsinco.brewery.api.util.Holder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface IntegrationEvent extends EventStepProperty, DrunkEvent {

    /**
     * Simple way to run the event
     *
     * @param player The player target for event
     */
    void run(Holder.Player player);

    /**
     * Complex way to run the event
     *
     * @return An event executable
     */
    default @NonNull EventPropertyExecutable toExecutable() {
        return new EventPropertyExecutable() {

            @Override
            public ExecutionResult execute(UUID contextPlayer, List<EventStepProperty> remaining) {
                run(new Holder.Player(contextPlayer));
                return ExecutionResult.CONTINUE;
            }

            @Override
            public ExecutionOutcome executeFor(UUID contextPlayer) {
                run(new Holder.Player(contextPlayer));
                return new ExecutionOutcome.Continue();
            }

            @Override
            public int priority() {
                return 42; // The meaning of life
            }

            @Override
            public EventPropertyExecutable withSkipPoint(@Nullable EventPropertyExecutable point) {
                return null;
            }

            @Override
            public EventStepProperty toProperty() {
                return IntegrationEvent.this;
            }
        };
    }
}
