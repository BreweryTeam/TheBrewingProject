package dev.jsinco.brewery.bukkit.effect.step;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.api.event.step.Condition;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ConditionalStepExecutable implements EventPropertyExecutable {

    private final Condition condition;

    public ConditionalStepExecutable(Condition condition) {
        this.condition = condition;
    }


    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.STOP_EXECUTION;
        }
        boolean cancel = switch (condition) {
            case Condition.Died died -> !player.isDead();
            case Condition.HasPermission hasPermission -> !player.hasPermission(hasPermission.permission());
            case Condition.JoinedServer joinedServer -> true;
            case Condition.JoinedWorld joinedWorld -> player.getWorld().getName().equals(joinedWorld.worldName());
            case Condition.TookDamage tookDamage -> false;
        };
        return cancel ? ExecutionResult.STOP_EXECUTION : ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return Integer.MIN_VALUE;
    }
}
