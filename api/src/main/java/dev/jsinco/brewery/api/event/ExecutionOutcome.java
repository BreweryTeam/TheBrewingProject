package dev.jsinco.brewery.api.event;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public sealed interface ExecutionOutcome {

    record Continue() implements ExecutionOutcome {

    }

    /**
     * Wait the specified amount of ticks.
     *
     * @param ticks The time in ticks
     */
    record Wait(long ticks) implements ExecutionOutcome {

    }

    /**
     *
     * @param skipPast The executable to skip past, or null to not skip
     */
    record Skip(@Nullable EventPropertyExecutable skipPast) implements ExecutionOutcome {

    }

    record SkipAll() implements ExecutionOutcome {

    }

    /**
     *
     * @param executables The executables to insert
     */
    record InsertSteps(List<EventPropertyExecutable> executables) implements ExecutionOutcome {

    }

    /**
     * @param executablesConsumer The remaining event property executables
     */
    record WaitCondition(Consumer<List<EventPropertyExecutable>> executablesConsumer) implements ExecutionOutcome {

    }
}
