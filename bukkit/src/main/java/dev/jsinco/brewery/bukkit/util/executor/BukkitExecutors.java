package dev.jsinco.brewery.bukkit.util.executor;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.util.executor.BreweryTask;
import dev.jsinco.brewery.util.executor.Executors;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BukkitExecutors extends Executors<BukkitTask, ScheduledTask> {

    @Override
    public BukkitTask sync(Consumer<BreweryTask> task) {
        return bukkitRunnable(task).runTask(TheBrewingProject.getInstance());
    }

    @Override
    public BukkitTask syncLater(long delay, Consumer<BreweryTask> task) {
        return bukkitRunnable(task).runTaskLater(TheBrewingProject.getInstance(), delay);
    }

    @Override
    public BukkitTask syncRepeating(long delay, long period, Consumer<BreweryTask> task) {
        return bukkitRunnable(task).runTaskTimer(TheBrewingProject.getInstance(), delay, period);
    }

    @Override
    public ScheduledTask async(Consumer<BreweryTask> task) {
        return Bukkit.getAsyncScheduler().runNow(TheBrewingProject.getInstance(), scheduledTask -> {
            task.accept(BukkitAsyncTask.from(scheduledTask));
        });
    }

    @Override
    public ScheduledTask asyncLater(TimeUnit unit, long delay, Consumer<BreweryTask> task) {
        return Bukkit.getAsyncScheduler().runDelayed(TheBrewingProject.getInstance(), scheduledTask -> {
            task.accept(BukkitAsyncTask.from(scheduledTask));
        }, delay, unit);
    }

    @Override
    public ScheduledTask asyncRepeating(TimeUnit unit, long delay, long period, Consumer<BreweryTask> task) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(TheBrewingProject.getInstance(), scheduledTask -> {
            task.accept(BukkitAsyncTask.from(scheduledTask));
        }, delay, period, unit);
    }

    // helper
    private BukkitRunnable bukkitRunnable(Consumer<BreweryTask> task) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                task.accept(BukkitGenericTask.from(this));
            }
        };
    }

}
