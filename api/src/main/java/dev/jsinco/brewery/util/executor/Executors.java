package dev.jsinco.brewery.util.executor;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public abstract class Executors<T, K> {

    private static Executors<?, ?> instance;

    public abstract T sync(Consumer<BreweryTask> task);
    public abstract T syncLater(long delay, Consumer<BreweryTask> task);
    public abstract T syncRepeating(long delay, long period, Consumer<BreweryTask> task);

    public abstract K async(Consumer<BreweryTask> task);
    public abstract K asyncLater(TimeUnit unit, long delay, Consumer<BreweryTask> task);
    public abstract K asyncRepeating(TimeUnit unit, long delay, long period, Consumer<BreweryTask> task);

    public T sync(Runnable task) {
        return sync((t) -> task.run());
    }
    public T syncLater(long delay, Runnable task) {
        return syncLater(delay, (t) -> task.run());
    }
    public T syncRepeating(long delay, long period, Runnable task) {
        return syncRepeating(delay, period, (t) -> task.run());
    }
    public K async(Runnable task) {
        return async((k) -> task.run());
    }
    public K asyncLater(TimeUnit unit, long delay, Runnable task) {
        return asyncLater(unit, delay, (k) -> task.run());
    }
    public K asyncRepeating(TimeUnit unit, long delay, long period, Runnable task) {
        return asyncRepeating(unit, delay, period, (k) -> task.run());
    }

    @SuppressWarnings("unchecked")
    public static <T, K> Executors<T, K> getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Executors instance is not set.");
        }
        return (Executors<T, K>) instance;
    }

    public static <T, K> void setInstance(Executors<T, K> newInstance) {
        instance = newInstance;
    }
}
