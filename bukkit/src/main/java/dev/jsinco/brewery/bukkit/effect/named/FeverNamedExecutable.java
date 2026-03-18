package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.api.event.EventStep;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.configuration.EventSection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public class FeverNamedExecutable implements EventPropertyExecutable {

    @Override
    public @NonNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }

        new BukkitRunnable() {
            int ticksRan = 0;

            @Override
            public void run() {
                if (ticksRan++ >= EventSection.events().feverFreezingTime().durationTicks()) {
                    cancel();
                    return;
                }
                if (!player.isOnline()) {
                    return;
                }
                player.setFreezeTicks(player.getMaxFreezeTicks());
            }
        }.runTaskTimer(TheBrewingProject.getInstance(), 0, 1);
        player.setFireTicks((int) EventSection.events().feverBurnTime().durationTicks());
        return ExecutionResult.CONTINUE;
    }

    @Override
    public int priority() {
        return -1;
    }

}
