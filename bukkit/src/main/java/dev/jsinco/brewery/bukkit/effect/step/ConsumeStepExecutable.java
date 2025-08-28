package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ConsumeStepExecutable implements EventPropertyExecutable {

    private final int alcohol;
    private final int toxins;

    public ConsumeStepExecutable(int alcohol, int toxins) {
        this.alcohol = alcohol;
        this.toxins = toxins;
    }

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        TheBrewingProject.getInstance().getDrunksManager().consume(contextPlayer, alcohol, toxins);
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return 2;
    }

}
