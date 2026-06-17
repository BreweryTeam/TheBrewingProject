package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStepProperty;
import dev.jsinco.brewery.api.event.ExecutionOutcome;
import dev.jsinco.brewery.api.event.step.Condition;
import dev.jsinco.brewery.api.event.step.ConditionalStep;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record ConditionalStepExecutable(Condition condition,
                                        @Nullable EventPropertyExecutable skipPoint) implements EventPropertyExecutable {

    public ConditionalStepExecutable(Condition condition) {
        this(condition, null);
    }

    @Override
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<EventStepProperty> eventStepProperties) {
        return shouldCancel(contextPlayer, condition) ? ExecutionResult.STOP_EXECUTION : ExecutionResult.CONTINUE;
    }

    @Override
    public ExecutionOutcome executeFor(UUID contextPlayer) {
        return shouldCancel(contextPlayer, condition) ?
                new ExecutionOutcome.Skip(skipPoint)
                : new ExecutionOutcome.Continue();
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public EventStepProperty toProperty() {
        return new ConditionalStep(condition);
    }

    @Override
    public ExecutionContext context() {
        return ExecutionContext.PLAYER;
    }

    @Override
    public EventPropertyExecutable withSkipPoint(@Nullable EventPropertyExecutable point) {
        return new ConditionalStepExecutable(condition, point);
    }

    private boolean shouldCancel(UUID contextPlayer, Condition condition) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return true;
        }
        return switch (condition) {
            case Condition.Died ignored -> !player.isDead();
            case Condition.HasPermission hasPermission -> !player.hasPermission(hasPermission.permission());
            case Condition.JoinedServer ignored -> false;
            case Condition.JoinedWorld joinedWorld -> !player.getWorld().getName().equals(joinedWorld.worldName());
            case Condition.TookDamage ignored -> true;
            case Condition.ModifierAbove modifierAbove -> {
                DrunkState state = TheBrewingProject.getInstance().getDrunksManager().getDrunkState(contextPlayer);
                if (state == null) {
                    yield true;
                }
                yield state.modifierValue(modifierAbove.modifier()) < modifierAbove.value();
            }
            case Condition.NotCondition notCondition -> !shouldCancel(contextPlayer, notCondition.toInvert());
        };
    }

}
