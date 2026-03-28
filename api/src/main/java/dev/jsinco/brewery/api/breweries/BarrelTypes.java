package dev.jsinco.brewery.api.breweries;

import dev.jsinco.brewery.api.util.BreweryKey;
import dev.jsinco.brewery.api.util.BreweryRegistry;

public class BarrelTypes {

    public static BarrelType ANY = get("any");
    public static BarrelType OAK = get("oak");
    public static BarrelType BIRCH = get("birch");
    public static BarrelType SPRUCE = get("spruce");
    public static BarrelType JUNGLE = get("jungle");
    public static BarrelType ACACIA = get("acacia");
    public static BarrelType DARK_OAK = get("dark_oak");
    public static BarrelType CRIMSON = get("crimson");
    public static BarrelType WARPED = get("warped");
    public static BarrelType CHERRY = get("cherry");
    public static BarrelType BAMBOO = get("bamboo");
    public static BarrelType MANGROVE = get("mangrove");
    public static BarrelType PALE_OAK = get("pale_oak");
    public static BarrelType COPPER = get("copper");



    private static BarrelType get(String name) {
        return BreweryRegistry.BARREL_TYPE.get(BreweryKey.parse(name));
    }
}
