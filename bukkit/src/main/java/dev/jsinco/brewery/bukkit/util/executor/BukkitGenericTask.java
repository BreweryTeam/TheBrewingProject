package dev.jsinco.brewery.bukkit.util.executor;

import dev.jsinco.brewery.util.executor.BreweryTask;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class BukkitGenericTask implements BreweryTask {

    private final BukkitRunnable runnable;

    public BukkitGenericTask(BukkitRunnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void cancel() {
        runnable.cancel();
    }

    @Override
    public boolean isCancelled() {
        return runnable.isCancelled();
    }

    @NotNull
    @Override
    public Integer getTaskId() {
        return runnable.getTaskId();
    }

    @Override
    public boolean isAsync() {
        try {
            // No idea why this field is private in Bukkit
            Field field = BukkitRunnable.class.getDeclaredField("task");
            field.setAccessible(true);
            BukkitTask bukkitTask = (BukkitTask) field.get(runnable);
            if (bukkitTask != null) {
                return !bukkitTask.isSync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static BreweryTask from(BukkitRunnable task) {
        return new BukkitGenericTask(task);
    }
}
