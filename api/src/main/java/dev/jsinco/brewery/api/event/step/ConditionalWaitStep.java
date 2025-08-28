package dev.jsinco.brewery.api.event.step;

import dev.jsinco.brewery.api.event.EventStepProperty;

import java.util.Locale;

public record ConditionalWaitStep(Condition condition) implements EventStepProperty {

    public static ConditionalWaitStep parse(String condition) {
        return new ConditionalWaitStep(Condition.valueOf(condition.toUpperCase(Locale.ROOT)));
    }

    public Condition getCondition() {
        return condition;
    }

    public enum Condition {
        JOIN
    }
}
