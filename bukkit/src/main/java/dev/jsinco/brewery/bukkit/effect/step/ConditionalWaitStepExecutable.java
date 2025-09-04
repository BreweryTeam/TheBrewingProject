package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.step.Condition;
import dev.jsinco.brewery.api.event.step.ConditionalWaitStep;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ConditionalWaitStepExecutable implements EventPropertyExecutable {

    private final Condition condition;

    public ConditionalWaitStepExecutable(Condition condition) {
        this.condition = condition;
    }


    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        EventStep.Builder builder = new EventStep.Builder();
        // As multiple event properties can be executed in the same step, we have to consider the remainder
        events.get(index).properties().stream()
                .filter(eventStepProperty -> !(eventStepProperty instanceof ConditionalWaitStep))
                .forEach(builder::addProperty);
        Stream<EventStep> eventStepStream = events.size() <= index + 1 ?
                Stream.of(builder.build()) :
                Stream.concat(
                        Stream.of(builder.build()),
                        events.subList(index + 1, events.size()).stream()
                );
        TheBrewingProject.getInstance().getDrunkEventExecutor().scheduleWait(contextPlayer, eventStepStream.toList(), condition);
        return ExecutionResult.STOP_EXECUTION;
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE;
    }
}
