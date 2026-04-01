package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.event.step.Condition;
import dev.jsinco.brewery.api.event.step.ConditionalWaitStep;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public class ConditionalWaitStepExecutable implements EventPropertyExecutable {

    private final Condition condition;

    public ConditionalWaitStepExecutable(Condition condition) {
        this.condition = condition;
    }


    @Override
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<EventStepProperty> eventStepProperties) {
        TheBrewingProject.getInstance().getDrunkEventExecutor().addConditionalWaitExecution(
                contextPlayer,
                eventStepProperties
                        .stream()
                        .map(eventStepProperty -> new EventStep.Builder()
                                .addProperty(eventStepProperty).build()
                        ).toList(),
                condition);
        return ExecutionResult.WAIT_UNTIL_CONDITION;
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE + 1;
    }

    @Override
    public EventStepProperty toProperty() {
        return new ConditionalWaitStep(condition);
    }
}
