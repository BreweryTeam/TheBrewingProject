package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.step.CustomEventCompleted;
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
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        List<EventStep> stepsToRun = new ArrayList<>(steps);
        ExecutionResult executionResult;
        if (events.size() == index + 2 && events.getLast().properties().stream().allMatch(CustomEventCompleted.class::isInstance)) {
            executionResult = ExecutionResult.CONTINUE;
        } else if (events.size() <= index + 1) {
            stepsToRun.addAll(events.subList(index, events.size() - 1));
            executionResult = ExecutionResult.WAIT_UNTIL_CONDITION;
        } else {
            executionResult = ExecutionResult.WAIT_UNTIL_CONDITION;
        }
        TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvents(contextPlayer, stepsToRun, newEventKey, true);
        return executionResult;
    }

    @Override
    public int priority() {
        return 3;
    }
}
