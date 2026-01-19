package dev.jsinco.brewery.api.event;

import dev.jsinco.brewery.api.util.Holder;

public interface IntegrationEvent extends EventStepProperty, DrunkEvent {

    void run(Holder.Player player);
}
