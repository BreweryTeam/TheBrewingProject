package dev.jsinco.brewery.api.event;


import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;

public interface EventPropertyExecutable {

    Random RANDOM = new Random();

    /**
     * @param contextPlayer A UUID of the player
     * @param events        The events to run for the player
     * @param index         Current index of the run events
     * @return Information whether further execution should occur
     */
    @NonNull
    ExecutionResult execute(UUID contextPlayer, List<? extends EventStep> events, int index);

    /**
     * Priority of the step, just used to make the execution order deterministic
     *
     * @return the priority of the step, lower values are executed first
     */
    int priority();

    default ExecutionContext context() {
        return new IndependantExecutionContext();
    }

    enum ExecutionResult {
        CONTINUE,
        STOP_EXECUTION,
        WAIT_UNTIL_CONDITION
    }

    interface ExecutionContext {

        Executor executor();

        boolean inheritsThread(ExecutionContext previous);

    }

    record IndependantExecutionContext() implements ExecutionContext {

        @Override
        public Executor executor() {
            return Runnable::run;
        }

        @Override
        public boolean inheritsThread(ExecutionContext previous) {
            return true;
        }
    }

}
