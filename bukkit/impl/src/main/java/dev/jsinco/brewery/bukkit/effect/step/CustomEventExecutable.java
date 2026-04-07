package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.event.step.CustomEventCompleted;
import dev.jsinco.brewery.api.event.step.CustomEventStep;
import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.jspecify.annotations.NonNull;

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
        List<EventStep> stepsToRun = new ArrayList<>(steps);
        ExecutionResult executionResult;
        if (eventStepProperties.stream().allMatch(CustomEventCompleted.class::isInstance)) {
            executionResult = ExecutionResult.CONTINUE;
        } else {
            executionResult = ExecutionResult.WAIT_UNTIL_CONDITION;
        }
        eventStepProperties.stream()
                .map(eventStepProperty -> new EventStep.Builder().addProperty(eventStepProperty).build())
                .forEach(stepsToRun::add);
        TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvents(contextPlayer, stepsToRun, newEventKey, true);
        return executionResult;
    }

    @Override
    public int priority() {
        return 3;
    }

    @Override
    public EventStepProperty toProperty() {
        return new CustomEventStep(newEventKey);
    }
}
