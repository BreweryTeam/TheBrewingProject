package dev.jsinco.brewery.breweries;


import dev.jsinco.brewery.util.BreweryKey;
import dev.jsinco.brewery.util.BreweryKeyed;

import java.util.Arrays;
import java.util.Locale;

public enum BarrelType implements BreweryKeyed {

    ANY,
    OAK,
    BIRCH,
    SPRUCE,
    JUNGLE,
    ACACIA,
    DARK_OAK,
    CRIMSON,
    WARPED,
    CHERRY,
    BAMBOO,
    PALE_OAK,
    COPPER;

    public static final BarrelType[] PLACEABLE_TYPES = Arrays.stream(values()).filter(barrelType -> barrelType != ANY).toArray(BarrelType[]::new);

    public BreweryKey key() {
        return BreweryKey.parse(name().toLowerCase(Locale.ROOT));
    }
}
