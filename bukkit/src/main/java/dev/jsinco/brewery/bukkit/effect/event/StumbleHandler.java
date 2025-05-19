package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Random;

class StumbleHandler {
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

    public void doStumble(BukkitTask task) {
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
