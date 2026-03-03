package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.configuration.EventSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FeverNamedExecutable implements EventPropertyExecutable {

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }
        AtomicInteger ticksRan = new AtomicInteger(0);
        player.getScheduler().runAtFixedRate(TheBrewingProject.getInstance(), task -> {
            if (ticksRan.get() == 0) {
                player.setFireTicks((int) EventSection.events().feverBurnTime().durationTicks());
            }
            if (ticksRan.incrementAndGet() >= EventSection.events().feverFreezingTime().durationTicks()) {
                task.cancel();
                return;
            }
            if (!player.isOnline()) {
                return;
            }
            player.setFreezeTicks(player.getMaxFreezeTicks());
        }, null, 1, 1);
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
