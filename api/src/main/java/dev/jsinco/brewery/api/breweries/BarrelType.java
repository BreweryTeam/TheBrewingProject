package dev.jsinco.brewery.api.breweries;


import dev.jsinco.brewery.api.util.BreweryKeyed;

public interface BarrelType extends BreweryKeyed {

    BarrelType ANY = BarrelTypeProviderHolder.builder("any")
            .globalProximity(1D)
            .build();
    BarrelType OAK = BarrelTypeProviderHolder.predefined("oak");
    BarrelType BIRCH = BarrelTypeProviderHolder.predefined("birch");
    BarrelType SPRUCE = BarrelTypeProviderHolder.predefined("spruce");
    BarrelType JUNGLE = BarrelTypeProviderHolder.predefined("jungle");
    BarrelType ACACIA = BarrelTypeProviderHolder.predefined("acacia");
    BarrelType DARK_OAK = BarrelTypeProviderHolder.predefined("dark_oak");
    BarrelType CRIMSON = BarrelTypeProviderHolder.predefined("crimson");
    BarrelType WARPED = BarrelTypeProviderHolder.predefined("warped");
    BarrelType CHERRY = BarrelTypeProviderHolder.predefined("cherry");
    BarrelType BAMBOO = BarrelTypeProviderHolder.predefined("bamboo");
    BarrelType MANGROVE = BarrelTypeProviderHolder.predefined("mangrove");
    BarrelType PALE_OAK = BarrelTypeProviderHolder.predefined("pale_oak");
    BarrelType COPPER = BarrelTypeProviderHolder.predefined("copper");

    /**
     * Evaluate the proximity of another barrel type.
     *
     * @param other Another barrel type
     * @return A score between 0 and 1
     */
    double proximityScore(BarrelType other);

    interface Builder {


        Builder addProximity(BarrelType barrelType, double scoreMultiplier);

        Builder globalProximity(double scoreMultiplier);

        BarrelType build();
    }
}
