package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.event.EventPropertyExecutable;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.util.executor.Executors;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class WaitStepExecutable implements EventPropertyExecutable {

    private final int durationTicks;

    public WaitStepExecutable(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        if (index + 1 >= events.size()) {
            return ExecutionResult.STOP_EXECUTION;
        }

        final List<EventStep> eventsLeft = events.subList(index + 1, events.size());
        Executors.getInstance().syncLater(durationTicks, () -> {
            TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvents(contextPlayer, eventsLeft);
        });
        return ExecutionResult.STOP_EXECUTION;
    }

}
