package dev.jsinco.brewery.bukkit.effect.event;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.effect.DrunkState;
import dev.jsinco.brewery.effect.DrunksManager;
import dev.jsinco.brewery.event.NamedDrunkEvent;
import dev.jsinco.brewery.util.Logging;
import dev.jsinco.brewery.util.Pair;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.Random;

class DrunkenWalkHandler {

    private final DrunksManager drunksManager;
    private final LinkedList<Pair<Vector, Integer>> vectors;
    private int currentTimestamp;
    private int duration;
    private int timestamp = 0;
    private final Player player;
    private Vector currentPush;

    private static int DIRECTION_INTERVAL = 40;
    private static double MINIMUM_PUSH_MAGNITUDE = 0.1;
    private static double MAXIMUM_PUSH_MAGNITUDE = 0.4;
    private static final Random RANDOM = new Random();

    DrunkenWalkHandler(int duration, Player player, DrunksManager drunksManager) {
        this.duration = duration;
        this.player = player;
        this.drunksManager = drunksManager;
        this.vectors = compileRandomVectors(duration);
        pollNewCurrentVector(vectors);
    }

    private void pollNewCurrentVector(LinkedList<Pair<Vector, Integer>> vectors) {
        Pair<Vector, Integer> pair = vectors.poll();
        this.currentPush = pair == null ? null : pair.first();
        this.currentTimestamp = pair == null ? 0 : pair.second();
    }

    private LinkedList<Pair<Vector, Integer>> compileRandomVectors(int duration) {
        int amount = duration / DIRECTION_INTERVAL;
        LinkedList<Pair<Vector, Integer>> output = new LinkedList<>();
        for (int i = 0; i < amount; i++) {
            double angle = RANDOM.nextDouble(Math.PI * 2);
            double radius = RANDOM.nextDouble(MINIMUM_PUSH_MAGNITUDE, MAXIMUM_PUSH_MAGNITUDE);
            Vector vector = new Vector(Math.cos(angle), 0, Math.sin(angle)).multiply(radius);
            output.add(new Pair<>(vector, i * DIRECTION_INTERVAL));
        }
        return output;
    }

    public void tick(BukkitTask task) {
        if (duration <= timestamp++ || currentPush == null) {
            task.cancel();
            return;
        }
        DrunkState drunkState = drunksManager.getDrunkState(player.getUniqueId());
        if (!player.isOnline() || !player.isOnGround() || drunkState == null || drunkState.walkSpeedSquared() == 0
                || TheBrewingProject.getInstance().getActiveEventsRegistry().hasActiveEvent(player.getUniqueId(), NamedDrunkEvent.STUMBLE)
        ) {
            return;
        }
        Pair<Vector, Integer> next = vectors.peek();
        if (next == null) {
            player.setVelocity(currentPush);
            return;
        }
        Vector nextPush = next.first();
        int nextTimestamp = next.second();
        if (nextTimestamp <= timestamp) {
            pollNewCurrentVector(vectors);
            player.setVelocity(currentPush);
            return;
        }
        double interpolation = (double) (nextTimestamp - timestamp) / (nextTimestamp - currentTimestamp);
        Vector newPush = currentPush.clone().multiply(interpolation).add(nextPush.clone().multiply(1D - interpolation));
        player.setVelocity(newPush);
    }
}
