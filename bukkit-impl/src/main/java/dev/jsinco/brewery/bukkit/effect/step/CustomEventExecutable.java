package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.event.ExecutionOutcome;
import dev.jsinco.brewery.api.event.step.CustomEventStep;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.bukkit.effect.event.DrunkEventExecutor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomEventExecutable implements EventPropertyExecutable {

    private final List<EventStep> steps;
    private final BreweryKey newEventKey;

    public CustomEventExecutable(List<EventStep> steps, BreweryKey newEventKey) {
        this.steps = steps;
        this.newEventKey = newEventKey;
    }

    @Override
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<EventStepProperty> eventStepProperties) {
        executeFor(contextPlayer);
        return ExecutionResult.CONTINUE;
    }

    @Override
    public ExecutionOutcome executeFor(UUID contextPlayer) {
        List<EventPropertyExecutable> stepsToRun = new ArrayList<>(DrunkEventExecutor.unwrap(steps));
        if (stepsToRun.isEmpty()) {
            return new ExecutionOutcome.Continue();
        }
        EventPropertyExecutable last = stepsToRun.getLast();
        List<EventPropertyExecutable> output = stepsToRun.stream()
                .map(executable -> executable == last ? executable : executable.withSkipPoint(last))
                .toList();
        return new ExecutionOutcome.InsertSteps(output);
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public EventPropertyExecutable withSkipPoint(@Nullable EventPropertyExecutable point) {
        return this; // NO-OP
    }

    @Override
    public EventStepProperty toProperty() {
        return new CustomEventStep(newEventKey);
    }
}
