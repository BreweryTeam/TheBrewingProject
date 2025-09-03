package dev.jsinco.brewery.api.event;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import dev.jsinco.brewery.api.util.BreweryKey;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CustomEvent {

    private final List<EventStep> steps;
    private final Component displayName;
    private final EventProbability probability;

    public CustomEvent(List<EventStep> steps, @Nullable Component displayName, EventProbability probability) {
        this.steps = steps;
        this.displayName = displayName;
        this.probability = probability;
    }

    public Component displayName() {
        return displayName == null ? Component.text("?") : displayName;
    }

    public List<EventStep> getSteps() {
        return List.copyOf(steps);
    }

    public EventProbability probability() {
        return probability;
    }

    /**
     * Due to a deserialization limitation with okaeri, this class has to exist
     *
     * @param event
     * @param key
     */
    public record Keyed(CustomEvent event, BreweryKey key) implements DrunkEvent {

        @Override
        public Component displayName() {
            return event.displayName == null ? Component.text(key.key()) : event.displayName;
        }

        @Override
        public EventProbability probability() {
            return event.probability;
        }

        public List<EventStep> getSteps() {
            return event.getSteps();
        }
    }

    public static class Builder {
        private final ImmutableList.Builder<EventStep> steps = new ImmutableList.Builder<>();
        private Component displayName;
        private EventProbability probability = EventProbability.NONE;

        public Builder addStep(EventStep step) {
            steps.add(step);
            return this;
        }

        public Builder displayName(Component displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder probability(EventProbability probability) {
            this.probability = probability;
            return this;
        }

        public CustomEvent build() {
            List<EventStep> builtSteps = steps.build();
            Preconditions.checkArgument(!builtSteps.isEmpty(), "Steps cannot be empty");
            return new CustomEvent(builtSteps, displayName, probability);
        }

        public CustomEvent.Keyed build(BreweryKey key) {
            return new Keyed(build(), key);
        }
    }
}
