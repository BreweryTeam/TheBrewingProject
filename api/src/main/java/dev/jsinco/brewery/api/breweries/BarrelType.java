package dev.jsinco.brewery.api.breweries;


import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;

import java.util.Locale;

public interface BarrelType extends BreweryKeyed {

    BarrelType ANY = BarrelTypeProvider.builderStatic("any")
            .defaultProximity(1D)
            .build();
    BarrelType OAK = BarrelTypeProvider.predefinedStatic("oak");
    BarrelType BIRCH = BarrelTypeProvider.predefinedStatic("birch");
    BarrelType SPRUCE = BarrelTypeProvider.predefinedStatic("spruce");
    BarrelType JUNGLE = BarrelTypeProvider.predefinedStatic("jungle");
    BarrelType ACACIA = BarrelTypeProvider.predefinedStatic("acacia");
    BarrelType DARK_OAK = BarrelTypeProvider.predefinedStatic("dark_oak");
    BarrelType CRIMSON = BarrelTypeProvider.predefinedStatic("crimson");
    BarrelType WARPED = BarrelTypeProvider.predefinedStatic("warped");
    BarrelType CHERRY = BarrelTypeProvider.predefinedStatic("cherry");
    BarrelType BAMBOO = BarrelTypeProvider.predefinedStatic("bamboo");
    BarrelType MANGROVE = BarrelTypeProvider.predefinedStatic("mangrove");
    BarrelType PALE_OAK = BarrelTypeProvider.predefinedStatic("pale_oak");
    BarrelType COPPER = BarrelTypeProvider.predefinedStatic("copper");

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
