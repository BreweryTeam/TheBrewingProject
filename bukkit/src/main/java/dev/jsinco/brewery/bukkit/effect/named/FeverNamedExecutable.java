package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.event.EventPropertyExecutable;
import dev.jsinco.brewery.event.EventStep;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class FeverNamedExecutable implements EventPropertyExecutable {

    private static final int AFFECT_DURATION = 200; // 10 seconds

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }

        new BukkitRunnable() {
            int ticksRan = 0;

            @Override
            public void run() {
                if (ticksRan++ >= AFFECT_DURATION) {
                    cancel();
                    return;
                }
                player.setFreezeTicks(player.getMaxFreezeTicks());
            }
        }.runTaskTimer(TheBrewingProject.getInstance(), 0, 1);
        player.setFireTicks(AFFECT_DURATION / 2);
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
