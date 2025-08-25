package dev.jsinco.brewery.event;

import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.WeightedProbabilityElement;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;


public interface DrunkEvent extends WeightedProbabilityElement {

    /**
     * @return The minimal alcohol requirement to activate this event randomly
     */
    @ApiStatus.Experimental
    int alcoholRequirement();

    /**
     * @return The minimal toxin requirement to activate this event randomly
     */
    @ApiStatus.Experimental
    int toxinsRequirement();

    /**
     * A key identifying this event
     */
    BreweryKey key();

    /**
     * @return A display name for this event
     */
    Component displayName();
}
