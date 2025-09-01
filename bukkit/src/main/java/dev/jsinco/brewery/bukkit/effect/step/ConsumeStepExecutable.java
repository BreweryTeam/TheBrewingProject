package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.effect.ModifierConsume;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ConsumeStepExecutable implements EventPropertyExecutable {

    private final DrunkenModifier modifier;
    private final double incrementValue;

    public ConsumeStepExecutable(DrunkenModifier modifier, double incrementValue) {
        this.modifier = modifier;
        this.incrementValue = incrementValue;
    }

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        TheBrewingProject.getInstance().getDrunksManager().consume(contextPlayer, new ModifierConsume(modifier, incrementValue));
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return 2;
    }

}
