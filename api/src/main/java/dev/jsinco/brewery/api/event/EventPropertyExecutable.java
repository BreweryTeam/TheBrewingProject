package dev.jsinco.brewery.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public interface EventPropertyExecutable {

    Random RANDOM = new Random();

    /**
     * @param contextPlayer A UUID of the player
     * @param events        The events to run for the player
     * @param index         Current index of the run events
     * @return Information whether further execution should occur
     */
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
