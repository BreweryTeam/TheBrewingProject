package dev.jsinco.brewery.bukkit.util.executor;

import dev.jsinco.brewery.util.executor.BreweryTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class BukkitAsyncTask implements BreweryTask {

    private final ScheduledTask task;

    public BukkitAsyncTask(ScheduledTask task) {
        this.task = task;
    }

    @Override
    public void cancel() {
        task.cancel();
    }

    @Override
    public boolean isCancelled() {
        return task.isCancelled();
    }

    @Override
    public Integer getTaskId() {
        return null;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    public static BreweryTask from(ScheduledTask task) {
        return new BukkitAsyncTask(task);
    }
}
