package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
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
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        if (index + 1 >= events.size()) {
            return ExecutionResult.STOP_EXECUTION;
        }

        final List<? extends EventStep> eventsLeft = events.subList(index + 1, events.size());
        Bukkit.getGlobalRegionScheduler().runDelayed(TheBrewingProject.getInstance(), ignored ->
                TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvents(contextPlayer, eventsLeft), durationTicks);
        return ExecutionResult.STOP_EXECUTION;
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE;
    }

}
