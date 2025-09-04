package dev.jsinco.brewery.api.event.step;

import dev.jsinco.brewery.api.event.EventStepProperty;

public record ConditionalWaitStep(Condition condition) implements EventStepProperty {

    public Condition getCondition() {
        return condition;
    }
}
