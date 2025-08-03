package dev.jsinco.brewery.event;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import dev.jsinco.brewery.util.BreweryKey;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CustomEvent {

    private final List<EventStep> steps;
    private final int alcohol;
    private final int toxins;
    private final int probabilityWeight;
    private final Component displayName;

    public CustomEvent(List<EventStep> steps, int alcohol, int toxins, int probabilityWeight, @Nullable String displayName) {
        this.steps = steps;
        this.alcohol = alcohol;
        this.toxins = toxins;
        this.probabilityWeight = probabilityWeight;
        this.displayName = Component.text(displayName == null ? "Unknown" : displayName);
    }

    public int alcoholRequirement() {
        return alcohol;
    }

    public int toxinsRequirement() {
        return toxins;
    }

    public Component displayName() {
        return displayName;
    }

    public int probabilityWeight() {
        return probabilityWeight;
    }

    public List<EventStep> getSteps() {
        return List.copyOf(steps);
    }

    /**
     * Due to a deserialization limitation with okaeri, this class has to exist
     *
     * @param event
     * @param key
     */
    public record Keyed(CustomEvent event, BreweryKey key) implements DrunkEvent {

        @Override
        public int alcoholRequirement() {
            return event.alcoholRequirement();
        }

        @Override
        public int toxinsRequirement() {
            return event.toxinsRequirement();
        }

        @Override
        public Component displayName() {
            return event.displayName();
        }

        @Override
        public int probabilityWeight() {
            return event.probabilityWeight();
        }

        public List<EventStep> getSteps() {
            return event.getSteps();
        }
    }

    public static class Builder {
        private final ImmutableList.Builder<EventStep> steps = new ImmutableList.Builder<>();
        private int alcohol = 0;
        private int toxins = 0;
        private int probabilityWeight = 0;
        private String displayName;

        public Builder addStep(EventStep step) {
            steps.add(step);
            return this;
        }

        public Builder alcoholRequirement(int alcoholRequirement) {
            alcohol = alcoholRequirement;
            return this;
        }

        public Builder toxinsRequirement(int toxinsRequirement) {
            toxins = toxinsRequirement;
            return this;
        }

        public Builder probabilityWeight(int probabilityWeight) {
            this.probabilityWeight = probabilityWeight;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public CustomEvent build() {
            List<EventStep> builtSteps = steps.build();
            Preconditions.checkArgument(!builtSteps.isEmpty(), "Steps cannot be empty");
            return new CustomEvent(builtSteps, alcohol, toxins, probabilityWeight, displayName);
        }

        public CustomEvent.Keyed build(BreweryKey key) {
            return new Keyed(build(), key);
        }
    }
}
