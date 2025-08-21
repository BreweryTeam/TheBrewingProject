package dev.jsinco.brewery.event;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface EventStep {

    Set<EventStepProperty> properties();

    class Builder {

        ImmutableSet.Builder<EventStepProperty> propertyBuilder = new ImmutableSet.Builder<>();

        public Builder addProperty(@NotNull EventStepProperty property) {
            Preconditions.checkNotNull(property);
            propertyBuilder.add(property);
            return this;
        }

        public EventStep build() {
            return new EventStepImpl(propertyBuilder.build());
        }

        private record EventStepImpl(Set<EventStepProperty> properties) implements EventStep {

        }

    }


}
