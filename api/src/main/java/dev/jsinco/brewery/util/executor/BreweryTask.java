package dev.jsinco.brewery.util.executor;

import org.jetbrains.annotations.Nullable;

public interface BreweryTask {

    /**
     * Cancels the task.
     * If the task is already cancelled, this method might throw an exception or do nothing.
     * Depending on implementation.
     */
    void cancel();

    /**
     * Checks if the task is cancelled.
     * @return true if the task is cancelled, false otherwise.
     */
    boolean isCancelled();

    /**
     * Gets the ID of the task.
     * @return the task ID, or null if the implementation does not support task IDs.
     */
    @Nullable Integer getTaskId();

    /**
     * Checks if the task is running asynchronously.
     * @return true if the task is running asynchronously, false if it is running synchronously.
     */
    boolean isAsync();

}
