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
     * @return
     */
    int priority();

    enum ExecutionResult {
        CONTINUE,
        STOP_EXECUTION
    }

}
