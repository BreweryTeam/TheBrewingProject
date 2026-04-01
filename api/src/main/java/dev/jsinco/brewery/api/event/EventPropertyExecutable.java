package dev.jsinco.brewery.api.event;


import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public interface EventPropertyExecutable {

    Random RANDOM = new Random();

    /**
     * @param contextPlayer A UUID of the player
     * @param remaining     The remaining event step properties to execute
     * @return Information whether further execution should occur
     */
    @NonNull
    ExecutionResult execute(UUID contextPlayer, List<EventStepProperty> remaining);

    /**
     * Priority of the step, just used to make the execution order deterministic
     *
     * @return the priority of the step, lower values are executed first
     */
    int priority();

    default ExecutionContext context() {
        return ExecutionContext.ANY;
    }

    EventStepProperty toProperty();

    enum ExecutionResult {
        CONTINUE,
        STOP_EXECUTION,
        WAIT_UNTIL_CONDITION
    }

    enum ExecutionContext {
        PLAYER, ANY
    }

}
