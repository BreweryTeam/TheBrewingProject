package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class CustomEventExecutable implements EventPropertyExecutable {

    private final List<EventStep> steps;

    public CustomEventExecutable(List<EventStep> steps) {
        this.steps = steps;
    }

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        TheBrewingProject.getInstance().getDrunkEventExecutor().doDrunkEvents(contextPlayer, steps);
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return 3;
    }
}
