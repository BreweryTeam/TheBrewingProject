package dev.jsinco.brewery.bukkit.animation;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;

import java.util.function.Consumer;

public class ItemAddAnimation implements Consumer<ScheduledTask> {

    private final Location from;
    private final Location to;
    private final Vector offsetDirection;
    private final ItemDisplay entity;
    private long time = 0;
    private static final double Y_MAX = 1D;
    private static final double X_END = 1D;
    private static final double G = 9.82;
    private static final double V0Y = Math.sqrt(Y_MAX * G * 2);
    private static final double V0X = G * X_END / (V0Y * 2);
    public static final long T_END = (long) Math.ceil(X_END / V0X * 20);

    public ItemAddAnimation(Location from, Location to, ItemDisplay entity) {
        this.to = to;
        this.entity = entity;
        double yawRadians = (from.getYaw() - 90) / 360 * Math.PI * 2;
        this.offsetDirection = new Vector(Math.cos(yawRadians), 0, Math.sin(yawRadians));
        this.from = to.clone().add(offsetDirection.clone().multiply(X_END));
        /*
         * Equation definitions are defined within square brackets []
         * Equation of movement under gravity in 2d
         * ymax = (v0y - g * tmax / 2 ) * tmax -> v0y = g * tmax / 2 + ymax / tmax [1]
         *
         * No velocity at top point y'(tmax) = 0
         * 0 = v0y - g * tmax -> tmax = v0y / g [2]
         *
         * These define the end point
         * tend = 2 * tmax [3]
         * xend = tend * v0x -> v0x = xend / tend[4]
         *
         * Assuming starting point is (0, 0)
         * Which means:
         *
         * [1 + 2]:
         * v0y = g * v0y / ( g * 2) + ymax * g / v0y
         * -> v0y^2 = v0y^2 / 2 + ymax * g
         * -> v0y^2 = ymax * g * 2
         * -> v0y = sqrt(ymax * g * 2) [5]
         *
         * [2 + 4]
         * v0x = xend / (2 * v0y / g)
         */
    }


    @Override
    public void accept(ScheduledTask scheduledTask) {
        if (time >= T_END) {
            entity.remove();
            scheduledTask.cancel();
            return;
        }
        Transformation previous = entity.getTransformation();
        entity.setTransformation(
                new Transformation(previous.getTranslation(), previous.getLeftRotation().rotationAxis(new AxisAngle4f((float) Math.PI * time / 10, 0, 1, 0)), previous.getScale(), previous.getRightRotation())
        );
        time++;
        double timeSeconds = time / 20D;
        Vector travelingPoint = offsetDirection.clone().multiply(-1);
        travelingPoint.multiply(timeSeconds * V0X).setY(
                (V0Y - G * timeSeconds / 2) * timeSeconds
        );
        entity.teleport(from.clone().add(travelingPoint));
    }
}
