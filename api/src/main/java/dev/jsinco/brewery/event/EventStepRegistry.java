package dev.jsinco.brewery.event;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A registry for {@link EventStep} classes that allows for "upgrading"
 * from one version to another. This class is mainly used for converting
 * EventStep instances to their child implementations which can be executed.
 */
public final class EventStepRegistry {

    @FunctionalInterface
    public interface EventStepFactory<T extends EventStepProperty> {
        EventPropertyExecutable create(T step);
    }

    public interface NamedEventStepFactory {
        EventPropertyExecutable create();
    }

    private final Map<Class<? extends EventStepProperty>, EventStepFactory<?>> factories = new HashMap<>();
    private final Map<NamedDrunkEvent, NamedEventStepFactory> namedFactories = new HashMap<>();

    public <T extends EventStepProperty> void register(Class<T> baseClass, EventStepFactory<T> factory) {
        if (factories.containsKey(baseClass)) {
            throw new IllegalArgumentException("Factory for " + baseClass.getName() + " is already registered.");
        }
        factories.put(baseClass, factory);
    }

    public void register(NamedDrunkEvent key, NamedEventStepFactory factory) {
        if (namedFactories.containsKey(key)) {
            throw new IllegalArgumentException("Factory for " + key + " is already registered.");
        }
        namedFactories.put(key, factory);
    }

    /**
     * @param step The event step property to convert
     * @param <T>  Event step property type
     * @return An executable for the event step property
     */
    public @NotNull <T extends EventStepProperty> EventPropertyExecutable toExecutable(T step) {
        if (step instanceof NamedDrunkEvent namedDrunkEvent) {
            return toExecutable(namedDrunkEvent);
        }

        EventStepFactory<T> factory = (EventStepFactory<T>) factories.get(step.getClass());
        Preconditions.checkArgument(factory != null, "No ExecutableEventStep found for EventStep: " + step.getClass().getName());
        return factory.create(step);
    }

    /**
     * @param step A preset drunken event
     * @return an executable for the preset drunken event
     */
    public @NotNull EventPropertyExecutable toExecutable(NamedDrunkEvent step) {
        NamedEventStepFactory factory = namedFactories.get(step);
        Preconditions.checkArgument(factory != null, "No ExecutableEventStep found for EventStep: " + step.getClass().getName());
        return factory.create();
    }
}

