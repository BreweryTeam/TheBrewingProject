package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.event.ExecutionOutcome;
import dev.jsinco.brewery.api.event.step.WaitStep;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class WaitStepExecutable implements EventPropertyExecutable {

    private final int durationTicks;

    public WaitStepExecutable(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    @Override
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<EventStepProperty> eventStepProperties) {
        return ExecutionResult.CONTINUE;
    }

    @Override
    public ExecutionOutcome executeFor(UUID contextPlayer) {
        return new ExecutionOutcome.Wait(durationTicks);
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public EventPropertyExecutable withSkipPoint(@Nullable EventPropertyExecutable point) {
        return this; //NO-OP
    }

    @Override
    public EventStepProperty toProperty() {
        return new WaitStep(durationTicks);
    }

}
