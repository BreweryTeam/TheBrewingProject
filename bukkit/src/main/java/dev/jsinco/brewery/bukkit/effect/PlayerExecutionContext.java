package dev.jsinco.brewery.bukkit.effect;

import dev.jsinco.brewery.api.event.EventPropertyExecutable;
import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.Executor;

public record PlayerExecutionContext(UUID playerUuid) implements EventPropertyExecutable.ExecutionContext {
    @Override
    public Executor executor() {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) {
            return Runnable::run;
        }
        return runnable ->
                player.getScheduler().run(
                        TheBrewingProject.getInstance(),
                        ignored -> runnable.run(),
                        runnable
                );
    }

    @Override
    public boolean inheritsThread(EventPropertyExecutable.ExecutionContext previous) {
        if (previous instanceof PlayerExecutionContext(UUID uuid)) {
            return uuid.equals(this.playerUuid);
        }
        return false;
    }
}
