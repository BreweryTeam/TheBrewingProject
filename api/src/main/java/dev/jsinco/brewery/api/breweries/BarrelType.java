package dev.jsinco.brewery.api.breweries;


import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;

import java.util.Locale;

public interface BarrelType extends BreweryKeyed {

    /**
     * Evaluate the proximity of another barrel type.
     *
     * @param other Another barrel type
     * @return A score between 0 and 1
     */
    double proximityScore(BarrelType other);

    default String name() {
        return key().minimalized()
                .toUpperCase(Locale.ROOT);
    }

    interface Builder {


        Builder addProximity(BarrelType barrelType, double scoreMultiplier);

        Builder addProximity(BreweryKey barrelTypeKey, double scoreMultiplier);

        Builder defaultProximity(double scoreMultiplier);

        BarrelType build();
    }
}
