package dev.jsinco.brewery.api.event;


import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public interface EventPropertyExecutable {

    Random RANDOM = new Random();

    /**
     * @param contextPlayer A UUID of the player
     * @param remaining     The remaining event step executables to execute
     * @return Information whether further execution should occur
     * @deprecated Use {@link #executeFor(UUID)} instead
     */
    @Deprecated(forRemoval = true)
    ExecutionResult execute(UUID contextPlayer, List<EventStepProperty> remaining);

    /**
     *
     * @param contextPlayer A UUID of the player
     * @return Information whether further execution should occur
     */
    default ExecutionOutcome executeFor(UUID contextPlayer) {
        return execute(contextPlayer, List.of()) == ExecutionResult.CONTINUE ?
                new ExecutionOutcome.Continue() : new ExecutionOutcome.SkipAll();
    }

    /**
     * Priority of the step, just used to make the execution order deterministic
     *
     * @return the priority of the step, lower values are executed first
     */
    int priority();

    default ExecutionContext context() {
        return ExecutionContext.ANY;
    }

    /**
     * Some event property executables return {@link ExecutionOutcome.Skip}, by running this method,
     * you can specify to which point that should be skipped past when running the executable.
     *
     * @param point The execution property to skip past, or null to not skip
     * @return A new event property executable
     */
    EventPropertyExecutable withSkipPoint(@Nullable EventPropertyExecutable point);

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
