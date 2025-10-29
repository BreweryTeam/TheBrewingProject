package dev.jsinco.brewery.api.breweries;


import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryKeyed;

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
    MANGROVE,
    PALE_OAK,
    COPPER;

    public static final BarrelType[] PLACEABLE_TYPES = Arrays.stream(values()).filter(barrelType -> barrelType != ANY).toArray(BarrelType[]::new);

    public BreweryKey key() {
        return BreweryKey.parse(name());
    }
}
