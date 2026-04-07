package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.event.step.CustomEventCompleted;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public record CustomEventCompletedExecutable(CustomEventCompleted eventCompleted) implements EventPropertyExecutable {
    @Override
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<EventStepProperty> remaining) {
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public CustomEventCompleted toProperty() {
        return eventCompleted;
    }
}
