package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.event.step.WaitStep;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public class WaitStepExecutable implements EventPropertyExecutable {

    private final int durationTicks;

    public WaitStepExecutable(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    @Override
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<EventStepProperty> eventStepProperties) {
        if (eventStepProperties.isEmpty()) {
            return ExecutionResult.STOP_EXECUTION;
        }
        Bukkit.getGlobalRegionScheduler().runDelayed(TheBrewingProject.getInstance(), ignored ->
                TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvents(contextPlayer, eventStepProperties
                                .stream()
                                .map(eventStepProperty -> new EventStep.Builder().addProperty(eventStepProperty).build())
                                .toList(),
                        null,
                        false
                ), durationTicks);
        return ExecutionResult.WAIT_UNTIL_CONDITION;
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public EventStepProperty toProperty() {
        return new WaitStep(durationTicks);
    }

}
