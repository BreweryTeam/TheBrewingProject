package dev.jsinco.brewery.api.breweries;


import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;
import dev.jsinco.brewery.api.util.BreweryRegistry;

import java.util.Locale;

public interface BarrelType extends BreweryKeyed {

    BarrelType ANY = get("any");
    BarrelType OAK = get("oak");
    BarrelType BIRCH = get("birch");
    BarrelType SPRUCE = get("spruce");
    BarrelType JUNGLE = get("jungle");
    BarrelType ACACIA = get("acacia");
    BarrelType DARK_OAK = get("dark_oak");
    BarrelType CRIMSON = get("crimson");
    BarrelType WARPED = get("warped");
    BarrelType CHERRY = get("cherry");
    BarrelType BAMBOO = get("bamboo");
    BarrelType MANGROVE = get("mangrove");
    BarrelType PALE_OAK = get("pale_oak");
    BarrelType COPPER = get("copper");

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

    private static BarrelType get(String name) {
        return BreweryRegistry.BARREL_TYPE.get(BreweryKey.parse(name));
    }

    interface Builder {


        Builder addProximity(BarrelType barrelType, double scoreMultiplier);

        Builder addProximity(BreweryKey barrelTypeKey, double scoreMultiplier);

        Builder defaultProximity(double scoreMultiplier);

        BarrelType build();
    }
}
