package dev.jsinco.brewery.event;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public interface EventPropertyExecutable {

    Random RANDOM = new Random();

    @NotNull ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index);

    /**
     * Priority of the step, just used to make the execution order deterministic
     *
     * @return the priority of the step, lower values are executed first
     */
    int priority();

    enum ExecutionResult {
        CONTINUE,
        STOP_EXECUTION
    }

}
