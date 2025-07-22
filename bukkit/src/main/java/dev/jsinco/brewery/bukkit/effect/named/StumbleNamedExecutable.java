package dev.jsinco.brewery.bukkit.effect.named;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.event.EventPropertyExecutable;
import dev.jsinco.brewery.event.EventStep;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.util.executor.BreweryTask;
import dev.jsinco.brewery.util.executor.Executors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class StumbleNamedExecutable implements EventPropertyExecutable {

    private static final int STUMBLE_DURATION = 10;

    @Override
    public @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index) {
        Player player = Bukkit.getPlayer(contextPlayer);
        if (player == null) {
            return ExecutionResult.CONTINUE;
        }

        int duration = RANDOM.nextInt(STUMBLE_DURATION / 2, STUMBLE_DURATION * 3 / 2 + 1);
        StumbleHandler stumbleHandler = new StumbleHandler(duration, player);
        TheBrewingProject.getInstance().getActiveEventsRegistry().registerActiveEvent(player.getUniqueId(), NamedDrunkEvent.fromKey("stumble"), duration);
        Executors.getInstance().syncRepeating(0, 1, stumbleHandler::doStumble);
        return ExecutionResult.CONTINUE;
    }

    static class StumbleHandler {
        private final Vector pushDirection2;
        private int countDown;
        private final int duration;
        private final Player player;
        private final Vector pushDirection1;
        private static final Random RANDOM = new Random();

        public StumbleHandler(int duration, Player player) {
            this.countDown = duration;
            this.duration = duration;
            this.player = player;
            double radians1 = RANDOM.nextDouble(Math.PI * 2);
            Vector walk = TheBrewingProject.getInstance().getPlayerWalkListener().getRegisteredMovement(player.getUniqueId());
            double maxMagnitude;
            if (walk == null) {
                maxMagnitude = 0.1;
            } else {
                maxMagnitude = Math.max(0.1, walk.length());
            }
            this.pushDirection1 = new Vector(Math.cos(radians1), 0, Math.sin(radians1))
                    .multiply(RANDOM.nextDouble(maxMagnitude));
            double radians2 = RANDOM.nextDouble(Math.PI * 2);
            this.pushDirection2 = new Vector(Math.cos(radians2), 0, Math.sin(radians2))
                    .multiply(RANDOM.nextDouble(maxMagnitude));
        }

        public void doStumble(BreweryTask task) {
            if (!player.isOnline() || countDown-- < 0) {
                task.cancel();
                return;
            }
            if (!player.isOnGround()) {
                return;
            }
            double progress = ((double) duration - (double) countDown) / duration;
            Vector pushDirection = pushDirection2.clone()
                    .multiply(progress)
                    .add(pushDirection1.clone().multiply(1 - progress));
            player.setVelocity(pushDirection);
        }
    }

    @Override
    public int priority() {
        return -1;
    }
}
