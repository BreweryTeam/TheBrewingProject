package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.step.Condition;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public class ConditionalStepExecutable implements EventPropertyExecutable {

    private final Condition condition;

    public ConditionalStepExecutable(Condition condition) {
        this.condition = condition;
    }


    @Override
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        return shouldCancel(contextPlayer, condition) ? ExecutionResult.STOP_EXECUTION : ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE;
    }

    private boolean shouldCancel(UUID contextPlayer, Condition condition) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return true;
        }
        return switch (condition) {
            case Condition.Died died -> !player.isDead();
            case Condition.HasPermission hasPermission -> !player.hasPermission(hasPermission.permission());
            case Condition.JoinedServer joinedServer -> false;
            case Condition.JoinedWorld joinedWorld -> !player.getWorld().getName().equals(joinedWorld.worldName());
            case Condition.TookDamage tookDamage -> true;
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
